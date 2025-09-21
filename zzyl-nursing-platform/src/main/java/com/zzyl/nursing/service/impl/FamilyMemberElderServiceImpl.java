package com.zzyl.nursing.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzyl.common.constant.CacheConstants;
import com.zzyl.common.utils.UserThreadLocal;
import com.zzyl.nursing.domain.*;
import com.zzyl.nursing.dto.BindFamilyMemberRequestDto;
import com.zzyl.nursing.dto.MemberListDto;
import com.zzyl.nursing.mapper.*;
import com.zzyl.nursing.vo.BindFamilyMemberVo;
import com.zzyl.nursing.vo.DeviceDataByDayOrWeekVo;
import com.zzyl.nursing.vo.FamilyElderVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.zzyl.nursing.service.IFamilyMemberElderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 老人-家属关联中间Service业务层处理
 *
 * @author alexis
 * @date 2025-09-20
 */
@Slf4j
@Service
public class FamilyMemberElderServiceImpl extends ServiceImpl<FamilyMemberElderMapper, FamilyMemberElder> implements IFamilyMemberElderService {
    @Autowired
    private FamilyMemberElderMapper familyMemberElderMapper;
    @Autowired
    private ElderMapper elderMapper;
    @Autowired
    private FamilyMemberMapper familyMemberMapper;
    @Autowired
    private BedMapper bedMapper;
    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;
    @Autowired
    private AlertRuleMapper alertRuleMapper;
    @Autowired
    private DeviceDataMapper deviceDataMapper;
    /**
     * 新增老人-家属关联中间
     *
     * @param bindFamilyMemberRequestDto 老人-家属关联中间
     * @return 结果
     */
    @Override
    public int insertFamilyMemberElder(BindFamilyMemberRequestDto bindFamilyMemberRequestDto) {
        //1.通过身份证,姓名确定老人id
        Elder elder = elderMapper.selectOne(new LambdaQueryWrapper<Elder>().eq(Elder::getIdCardNo, bindFamilyMemberRequestDto.getIdCard())
                .eq(Elder::getName, bindFamilyMemberRequestDto.getName()));
        Long elderId = elder.getId();
        //获取当前微信登录用户信息id
        //通过本地线程获取当前登录用户id
        Long userId = UserThreadLocal.getUserId();
        log.info("当前登录用户ID:{}", userId);
        log.info("当前登录用户具体信息{}", familyMemberMapper.selectById(userId));
        //将上述信息插入中间表
        FamilyMemberElder familyMemberElder = new FamilyMemberElder();
        familyMemberElder.setElderId(elderId);
        familyMemberElder.setFamilyMemberId(userId);
        familyMemberElder.setRemark(bindFamilyMemberRequestDto.getRemark());
        return familyMemberElderMapper.insert(familyMemberElder);
    }

    /**
     * 查询所有关联信息
     *
     * @return List<BindFamilyMemberVo>
     */
    @Override
    public List<BindFamilyMemberVo> selectAllFamilyMember() {
        //获取当前微信登录用户信息id
        Long userId = UserThreadLocal.getUserId();
        //通过当前登录用户id查询所有关联信息
        List<FamilyMemberElder> familyMemberElders = familyMemberElderMapper
                .selectList(new LambdaQueryWrapper<FamilyMemberElder>()
                        .eq(FamilyMemberElder::getFamilyMemberId, userId));
        List<BindFamilyMemberVo> bindFamilyMemberVos = new ArrayList<>();
        if (familyMemberElders != null && familyMemberElders.size() > 0) {
            for (FamilyMemberElder familyMemberElder : familyMemberElders) {
                // 创建VO对象
                BindFamilyMemberVo bindFamilyMemberVo = new BindFamilyMemberVo();
                // 设置VO属性
                bindFamilyMemberVo.setId(familyMemberElder.getId());
                bindFamilyMemberVo.setFamilyMemberId(familyMemberElder.getFamilyMemberId());
                bindFamilyMemberVo.setElderId(familyMemberElder.getElderId());

                // 根据老人ID查询老人信息，获取老人姓名
                Elder elder = elderMapper.selectById(familyMemberElder.getElderId());
                if (elder != null) {
                    bindFamilyMemberVo.setElderName(elder.getName());
                }
                // 将VO对象添加到列表中
                bindFamilyMemberVos.add(bindFamilyMemberVo);
            }
        }
        return bindFamilyMemberVos;
    }

    /**
     * 查询家属关联的老人列表
     *
     * @param dto 查询参数
     * @return 老人列表
     */
    @Override
    public List<FamilyElderVo> selectFamilyMemberList(MemberListDto dto) {
        // 创建分页对象
        Page<FamilyMemberElder> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        // 查询当前登录用户关联的老人-家属关系
        Long userId = UserThreadLocal.getUserId();
        LambdaQueryWrapper<FamilyMemberElder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FamilyMemberElder::getFamilyMemberId, userId);

        // 执行分页查询
        IPage<FamilyMemberElder> familyMemberElderPage = familyMemberElderMapper.selectPage(page, queryWrapper);

        // 构建返回结果
        List<FamilyElderVo> result = new ArrayList<>();
        if (familyMemberElderPage.getRecords() != null && !familyMemberElderPage.getRecords().isEmpty()) {
            for (FamilyMemberElder familyMemberElder : familyMemberElderPage.getRecords()) {
                FamilyElderVo vo = new FamilyElderVo();

                // 设置家属相关信息
                vo.setMid(String.valueOf(familyMemberElder.getId()));
                vo.setMremark(familyMemberElder.getRemark());

                // 查询老人信息
                Elder elder = elderMapper.selectById(familyMemberElder.getElderId());
                if (elder != null) {
                    vo.setElderId(String.valueOf(elder.getId()));
                    vo.setName(elder.getName());
                    vo.setImage(elder.getImage());

                    // 查询床位信息
                    if (elder.getBedId() != null) {
                        Bed bed = bedMapper.selectById(elder.getBedId());
                        if (bed != null) {
                            vo.setBedNumber(bed.getBedNumber());
                            // 查询房间类型信息
                            if (bed.getRoomId() != null) {
                                List<Room> roomTypes = roomMapper.selectList(
                                        new LambdaQueryWrapper<Room>().eq(Room::getId, bed.getRoomId())
                                );
                                if (!roomTypes.isEmpty()) {
                                    vo.setTypeName(roomTypes.get(0).getTypeName());
                                }
                            }
                        }
                    }
                    List<Device> devices = deviceMapper.selectList(
                            new LambdaQueryWrapper<Device>()
                                    .eq(Device::getBindingLocation, elder.getId())
                                    .eq(Device::getLocationType, 0) // 0：随身设备
                    );
                    if (!devices.isEmpty()) {
                        for (Device device : devices) {
                            vo.setIotId(device.getIotId());
                            vo.setDeviceName(device.getDeviceName());
                            vo.setProductKey(device.getProductKey());
                        }

                    }
                }

                result.add(vo);
            }
        }

        return result;
    }

    /**
     *  根据设备ID实时查询设备信息
     * @param iotId 设备ID
     * @return 设备属性状态数据
     */
    @Override
    public  DevicePropertyStatusData  selectDeviceInfoByiotId(String iotId) {
        // 获取当前设备的最后上报数据
        String str = (String) redisTemplate.opsForHash().get(CacheConstants.IOT_DEVICE_LAST_DATA, iotId);
        if (StrUtil.isNotEmpty(str)) {
            List<DeviceData> deviceData = JSONUtil.toList(str, DeviceData.class);
            List<PropertyStatusInfo> propertyStatusInfos = new ArrayList<>();
            // 创建属性状态数据对象
            DevicePropertyStatusData devicePropertyStatusData = new DevicePropertyStatusData();
            // 创建属性列表对象
            PropertyList propertyList = new PropertyList();
            for (DeviceData data : deviceData) {
                // 创建属性状态信息对象
                PropertyStatusInfo propertyStatusInfo = new PropertyStatusInfo();
                propertyStatusInfo.setDataType("int");
                propertyStatusInfo.setIdentifier(data.getFunctionId());

                AlertRule alertRule = null;
                try {
                    alertRule = alertRuleMapper.selectOne(new LambdaQueryWrapper<AlertRule>()
                            .eq(AlertRule::getProductKey, data.getProductKey())
                            .eq(AlertRule::getFunctionId, data.getFunctionId()));

                    if (alertRule == null) {
                        log.warn("未找到匹配的告警规则: productKey={}, functionId={}", data.getProductKey(), data.getFunctionId());
                        // 创建一个新的AlertRule对象来设置functionName
                        alertRule = new AlertRule();
                        alertRule.setFunctionName(data.getFunctionId());
                    }
                } catch (Exception e) {
                    log.error("查询告警规则时发生异常: productKey={}, functionId={}", data.getProductKey(), data.getFunctionId(), e);
                    // 创建一个新的AlertRule对象来设置functionName
                    if (alertRule == null) {
                        alertRule = new AlertRule();
                    }
                    alertRule.setFunctionName(data.getFunctionId());
                }
                propertyStatusInfo.setName(alertRule.getFunctionName());

                propertyStatusInfo.setTime(String.valueOf(data.getAlarmTime()));
                propertyStatusInfo.setUnit("");
                propertyStatusInfo.setValue(data.getDataValue());
                propertyStatusInfos.add(propertyStatusInfo);

            }
            propertyList.setPropertyStatusInfo(propertyStatusInfos);
            devicePropertyStatusData.setList(propertyList);

            return devicePropertyStatusData;
        }
        return null;
    }


    /**
     * 批量删除老人-家属关联中间
     *
     * @param id 需要删除的老人-家属关联中间主键
     * @return 结果
     */
    @Override
    public int deleteFamilyMemberElderById(String id)
    {
        return familyMemberElderMapper.deleteById(id);
    }


    /**
     * 按天统计查询设备数据
     * 
     * @param functionId 功能ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param iotId      设备ID
     * @return 每小时的数据列表，每3小时计算一次平均值，仅在每3小时的起始时间点显示计算结果
     */
    @Override
    public List<DeviceDataByDayOrWeekVo> queryDeviceDataListByDay(String functionId, Long startTime, Long endTime, String iotId) {
        // 查询指定时间范围内的设备数据
        List<DeviceData> deviceDataList = deviceDataMapper.selectList(new LambdaQueryWrapper<DeviceData>()
                .eq(DeviceData::getIotId, iotId)
                .eq(DeviceData::getFunctionId, functionId)
                .ge(DeviceData::getAlarmTime,
                        LocalDateTime.ofInstant(new Date(startTime).toInstant(), ZoneId.systemDefault()))
                .le(DeviceData::getAlarmTime,
                        LocalDateTime.ofInstant(new Date(endTime).toInstant(), ZoneId.systemDefault()))
        );

        // 创建24小时的时间点 (00:00, 01:00, ..., 23:00)，初始值都为0.0
        List<DeviceDataByDayOrWeekVo> deviceDataByDayOrWeekVos = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            DeviceDataByDayOrWeekVo vo = new DeviceDataByDayOrWeekVo();
            vo.setDateTime(String.format("%02d:00", i));
            vo.setDataValue(BigDecimal.ZERO);
            deviceDataByDayOrWeekVos.add(vo);
        }

        // 用于存储每个3小时时间段的统计数据
        BigDecimal[] periodSums = new BigDecimal[8];
        int[] periodCounts = new int[8];
        
        // 初始化数组
        for (int i = 0; i < 8; i++) {
            periodSums[i] = BigDecimal.ZERO;
            periodCounts[i] = 0;
        }

        // 按3小时分组统计数据
        for (DeviceData deviceData : deviceDataList) {


            LocalDateTime alarmTime = deviceData.getAlarmTime();
            if (alarmTime != null) {
                int hour = alarmTime.getHour();
                // 计算所属时间段 (0-2 => 0, 3-5 => 1, ..., 21-23 => 7)
                int periodIndex = hour / 3;
                String dataValue = deviceData.getDataValue();
                if (dataValue != null && !dataValue.isEmpty()) {
                    try {
                        BigDecimal value = new BigDecimal(dataValue);
                        // 累加数值到对应时间段
                        periodSums[periodIndex] = periodSums[periodIndex].add(value);
                        // 增加计数
                        periodCounts[periodIndex]++;
                    } catch (NumberFormatException e) {
                        // 忽略无效数值
                    }
                }
            }
        }
        
        // 计算每个3小时时间段的平均值，并只赋值给该时间段的起始小时
        for (int i = 0; i < 8; i++) {
            // 每个3小时时间段的起始小时 (0, 3, 6, ..., 21)
            int startHour = i * 3;
            if (periodCounts[i] > 0) {
                // 计算平均值，保留2位小数 4舍5入
                BigDecimal averageValue = periodSums[i].divide(new BigDecimal(periodCounts[i]), 2, RoundingMode.HALF_UP);
                // 只将平均值赋给该时间段的起始小时
                DeviceDataByDayOrWeekVo vo = deviceDataByDayOrWeekVos.get(startHour);
                vo.setDataValue(averageValue);
            }
        }
        
        return deviceDataByDayOrWeekVos;
    }

    /**
     * 按周统计查询设备数据
     * 
     * @param functionId 功能ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param iotId      设备ID
     * @return 每天的数据列表，按天计算平均值，一周7天数据
     */
    @Override
    public List<DeviceDataByDayOrWeekVo> queryDeviceDataListByWeek(String functionId, Long startTime, Long endTime, String iotId) {
        // 查询指定时间范围内的设备数据
        List<DeviceData> deviceDataList = deviceDataMapper.selectList(new LambdaQueryWrapper<DeviceData>()
                .eq(DeviceData::getIotId, iotId)
                .eq(DeviceData::getFunctionId, functionId)
                .ge(DeviceData::getAlarmTime,
                        LocalDateTime.ofInstant(new Date(startTime).toInstant(), ZoneId.systemDefault()))
                .le(DeviceData::getAlarmTime,
                        LocalDateTime.ofInstant(new Date(endTime).toInstant(), ZoneId.systemDefault()))
        );

        // 创建7天的时间点 (使用日期格式)
        List<DeviceDataByDayOrWeekVo> result = new ArrayList<>();
        
        // 用于存储每天的统计数据
        BigDecimal[] daySums = new BigDecimal[7];
        int[] dayCounts = new int[7];
        
        // 初始化数组
        for (int i = 0; i < 7; i++) {
            daySums[i] = BigDecimal.ZERO;
            dayCounts[i] = 0;
        }

        // 按天分组统计数据
        for (DeviceData deviceData : deviceDataList) {
            LocalDateTime alarmTime = deviceData.getAlarmTime();
            if (alarmTime != null) {
                // 计算是开始时间后的第几天 (0-6)
                long diffInMillies = alarmTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - startTime;
                int dayIndex = (int) (diffInMillies / (24 * 60 * 60 * 1000));
                // 确保索引在有效范围内
                if (dayIndex >= 0 && dayIndex < 7) {
                    String dataValue = deviceData.getDataValue();
                    if (dataValue != null && !dataValue.isEmpty()) {
                        try {
                            BigDecimal value = new BigDecimal(dataValue);
                            // 累加数值到对应天
                            daySums[dayIndex] = daySums[dayIndex].add(value);
                            // 增加计数
                            dayCounts[dayIndex]++;
                        } catch (NumberFormatException e) {
                            // 忽略无效数值
                        }
                    }
                }
            }
        }
        
        // 创建结果列表，使用日期格式
        LocalDateTime startDate = LocalDateTime.ofInstant(new Date(startTime).toInstant(), ZoneId.systemDefault());
        for (int i = 0; i < 7; i++) {
            DeviceDataByDayOrWeekVo vo = new DeviceDataByDayOrWeekVo();
            // 格式化日期为 "MM.dd"
            LocalDateTime currentDate = startDate.plusDays(i);
            vo.setDateTime(String.format("%02d.%02d", currentDate.getMonthValue(), currentDate.getDayOfMonth()));
            if (dayCounts[i] > 0) {
                // 计算平均值，保留2位小数 4舍5入
                BigDecimal averageValue = daySums[i].divide(new BigDecimal(dayCounts[i]), 2, RoundingMode.HALF_UP);
                vo.setDataValue(averageValue);
            } else {
                vo.setDataValue(BigDecimal.ZERO);
            }
            result.add(vo);
        }
        
        return result;
    }
}
