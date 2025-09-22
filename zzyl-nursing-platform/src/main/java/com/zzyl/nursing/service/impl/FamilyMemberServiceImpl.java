package com.zzyl.nursing.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzyl.common.constant.CacheConstants;
import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.common.exception.ServiceException;
import com.zzyl.common.utils.UserThreadLocal;
import com.zzyl.nursing.domain.DeviceData;
import com.zzyl.nursing.domain.Elder;
import com.zzyl.nursing.domain.FamilyMember;
import com.zzyl.nursing.domain.FamilyMemberElder;
import com.zzyl.nursing.dto.DeviceDataQueryDto;
import com.zzyl.nursing.dto.UserLoginRequestDto;
import com.zzyl.nursing.mapper.FamilyMemberMapper;
import com.zzyl.nursing.service.*;
import com.zzyl.nursing.vo.ElderInfoVo;
import com.zzyl.nursing.vo.LoginVo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 老人家属Service业务层处理
 *
 * @author alexis
 * @date 2025-09-13
 */
@Service
public class FamilyMemberServiceImpl extends ServiceImpl<FamilyMemberMapper, FamilyMember> implements IFamilyMemberService {
    @Autowired
    private FamilyMemberMapper familyMemberMapper;
    @Autowired
    private WechatService wechatService;
    @Value("${token.secret}")
    private String secret;
    @Autowired
    private IElderService elderService;
    @Autowired
    private IFamilyMemberElderService familyMemberElderService;
    @Autowired
    private IDeviceDataService deviceDataService;
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Override
    public LoginVo login(UserLoginRequestDto userLoginRequestDto) {
        //1.获取openid
        String openid = wechatService.getOpenid(userLoginRequestDto.getCode());
        //2.根据openid查询用户信息
        //  如果为空 创建新的用户对象，并赋值openid
        FamilyMember familyMember = familyMemberMapper.selectOne(new LambdaQueryWrapper<FamilyMember>().eq(FamilyMember::getOpenId, openid));
        if (ObjUtil.isEmpty(familyMember)) {
            familyMember = new FamilyMember();
            familyMember.setOpenId(openid);
        }
        //3.获取手机号
        String phone = wechatService.getPhone(userLoginRequestDto.getPhoneCode());
        familyMember.setPhone(phone);

        //4.新增或更新用户
        if (familyMember.getId() == null) {
            familyMember.setName(generateRealisticNickname());
            familyMemberMapper.insert(familyMember);
        } else {
            familyMemberMapper.updateById(familyMember);
        }
        //5.生成jwt并返回
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", familyMember.getId());
        claims.put("nickName", familyMember.getName());
        String token = Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, secret).compact();
        LoginVo loginVo = new LoginVo();
        loginVo.setToken(token);
        loginVo.setNickName(familyMember.getName());
        return loginVo;
    }

    @Override
    public void bindElder(Map<String, Object> params) {
        String idCard = params.get("idCard").toString();
        String name = params.get("name").toString();
        String remark = params.get("remark").toString();
        Elder elder = elderService.getOne(new LambdaQueryWrapper<Elder>()
                .eq(Elder::getIdCardNo, idCard));
        if (ObjUtil.isEmpty(elder)) {
            throw new ServiceException("未找到该老人");
        }
        //查询关系是否存在
        FamilyMemberElder familyMemberElderDb = familyMemberElderService.getOne(new LambdaQueryWrapper<FamilyMemberElder>().eq(FamilyMemberElder::getElderId, elder.getId()));
        if (ObjUtil.isNotEmpty(familyMemberElderDb)) {
            throw new ServiceException("该老人已绑定,请勿重复绑定");
        }
        //添加家人-老人关系
        FamilyMemberElder familyMemberElder = FamilyMemberElder.builder()
                .familyMemberId(UserThreadLocal.getUserId())
                .elderId(elder.getId()).build();
        familyMemberElder.setRemark(remark);
        familyMemberElderService.save(familyMemberElder);


    }

    @Override
    public List<Map<String, Object>> listElders() {
        Long familyMemberId = UserThreadLocal.getUserId();
        List<FamilyMemberElder> familyMemberElders = familyMemberElderService.list(new LambdaQueryWrapper<FamilyMemberElder>()
                .eq(FamilyMemberElder::getFamilyMemberId, familyMemberId));
        return familyMemberElders.stream().map(familyMemberElder -> {
            Long elderId = familyMemberElder.getElderId();
            String elderName = elderService.getById(elderId).getName();
            Map<String, Object> map = new HashMap<>();
            map.put("id", familyMemberElder.getId());
            map.put("elderId", elderId);
            map.put("elderName", elderName);
            map.put("familyMemberId", familyMemberElder.getFamilyMemberId());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ElderInfoVo> listByPage(Integer pageNum, Integer pageSize) {
        List<ElderInfoVo> list = familyMemberElderService.listByPage(pageNum, pageSize);
        return list;
    }

    @Override
    public AjaxResult queryDevicePropertyStatus(String iotId) {
        // 从Redis中获取设备的最新数据
        String str = (String) redisTemplate.opsForHash().get(CacheConstants.IOT_DEVICE_LAST_DATA, iotId);
        if (StrUtil.isNotEmpty(str)) {
            List<DeviceData> list = JSONUtil.toList(str, DeviceData.class);
            List<Map<String, Object>> propertyStatusInfo = list.stream().map(deviceData -> {
                Map<String, Object> map = new HashMap<>();
                String functionId = deviceData.getFunctionId();
                LocalDateTime alarmTime = deviceData.getAlarmTime();
                long timestamp = alarmTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
                String value = deviceData.getDataValue();
                map.put("dateType", getJavaDataType(value));
                map.put("identifier", functionId);
                map.put("name", functionId);
                map.put("time", timestamp);
                map.put("unit", null);
                map.put("value", value);
                return map;
            }).collect(Collectors.toList());
            AjaxResult ajaxResult = new AjaxResult();
            Map<String, Object> data = new HashMap<>();
            Map<String, Object> listMap = new HashMap<>();
            listMap.put("propertyStatusInfo", propertyStatusInfo);
            data.put("list", listMap);
            ajaxResult.put("msg", "操作成功");
            ajaxResult.put("code", 200);
            ajaxResult.put("data", data); // 正确设置data字段
            return ajaxResult;
        }
        return AjaxResult.error("未查询到数据");
    }

    @Override
    public List<Map<String, Object>> queryDeviceDataListByDay(DeviceDataQueryDto dto) {

        LocalDateTime startTime = LocalDateTimeUtil.of(dto.getStartTime());
        LocalDateTime endTime = LocalDateTimeUtil.of(dto.getEndTime());
        List<Map<String, Object>> list = deviceDataService.queryDeviceDataListByDay(dto.getIotId(), dto.getFunctionId(), startTime, endTime);
        return list;

    }

    @Override
    public List<Map<String, Object>> queryDeviceDataListByWeek(DeviceDataQueryDto dto) {
        LocalDateTime startTime = LocalDateTimeUtil.of(dto.getStartTime());
        LocalDateTime endTime = LocalDateTimeUtil.of(dto.getEndTime());
        List<Map<String, Object>> list = deviceDataService.queryDeviceDataListByWeek(dto.getIotId(), dto.getFunctionId(), startTime, endTime);
        return list;
    }

    /**
     * 判断设备数据值的Java数据类型
     *
     * @param value 数据值
     * @return Java数据类型字符串
     */
    private String getJavaDataType(String value) {
        if (value == null) {
            return "String";
        }

        // 判断是否为布尔类型
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return "Boolean";
        }

        // 判断是否为整数类型
        try {
            Integer.parseInt(value);
            return "Integer";
        } catch (NumberFormatException e) {
            // 继续检查其他类型
        }

        // 判断是否为长整型
        try {
            Long.parseLong(value);
            return "Long";
        } catch (NumberFormatException e) {
            // 继续检查其他类型
        }

        // 判断是否为浮点型
        try {
            Float.parseFloat(value);
            return "Float";
        } catch (NumberFormatException e) {
            // 继续检查其他类型
        }

        // 判断是否为双精度浮点型
        try {
            Double.parseDouble(value);
            return "Double";
        } catch (NumberFormatException e) {
            // 默认为字符串类型
        }

        return "String";
    }


    /**
     * 生成真实的随机用户昵称
     *
     * @return 真实感的用户昵称
     */
    private String generateRealisticNickname() {
        // 常见的用户名前缀
        String[] prefixes = {
                "爱心", "贴心", "孝心", "暖心", "细心",
                "小", "大", "老", "超级", "金牌",
                "阳光", "快乐", "幸福", "美好", "温馨"
        };

        // 中间部分
        String[] middles = {
                "护理", "照护", "陪伴", "守护", "关怀",
                "小助手", "好帮手", "贴心人", "好伙伴", "守护者",
                "妈妈", "爸爸", "儿子", "女儿", "孙子", "孙女",
                "阿姨", "叔叔", "姐姐", "哥哥", "妹妹", "弟弟"
        };

        // 可选的后缀数字或字母
        String[] suffixes = {
                "", "", "", // 75%概率不加后缀
                "001", "002", "003", "004", "005",
                "88", "66", "99", "123", "666",
                "A", "B", "C", "D", "E"
        };

        // 随机组合生成昵称
        String prefix = prefixes[(int) (Math.random() * prefixes.length)];
        String middle = middles[(int) (Math.random() * middles.length)];
        String suffix = suffixes[(int) (Math.random() * suffixes.length)];

        return prefix + middle + suffix;
    }


    /**
     * 查询老人家属
     *
     * @param id 老人家属主键
     * @return 老人家属
     */
    @Override
    public FamilyMember selectFamilyMemberById(Long id) {
        return familyMemberMapper.selectById(id);
    }

    /**
     * 查询老人家属列表
     *
     * @param familyMember 老人家属
     * @return 老人家属
     */
    @Override
    public List<FamilyMember> selectFamilyMemberList(FamilyMember familyMember) {
        return familyMemberMapper.selectFamilyMemberList(familyMember);
    }

    /**
     * 新增老人家属
     *
     * @param familyMember 老人家属
     * @return 结果
     */
    @Override
    public int insertFamilyMember(FamilyMember familyMember) {
        return familyMemberMapper.insert(familyMember);
    }

    /**
     * 修改老人家属
     *
     * @param familyMember 老人家属
     * @return 结果
     */
    @Override
    public int updateFamilyMember(FamilyMember familyMember) {
        return familyMemberMapper.updateById(familyMember);
    }

    /**
     * 批量删除老人家属
     *
     * @param ids 需要删除的老人家属主键
     * @return 结果
     */
    @Override
    public int deleteFamilyMemberByIds(Long[] ids) {
        return familyMemberMapper.deleteBatchIds(Arrays.asList(ids));
    }

    /**
     * 删除老人家属信息
     *
     * @param id 老人家属主键
     * @return 结果
     */
    @Override
    public int deleteFamilyMemberById(Long id) {
        return familyMemberMapper.deleteById(id);
    }


}
