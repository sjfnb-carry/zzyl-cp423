package com.zzyl.nursing.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzyl.common.exception.ServiceException;
import com.zzyl.common.utils.CodeGenerator;
import com.zzyl.common.utils.IdCardNoUtils;
import com.zzyl.common.utils.bean.BeanUtils;
import com.zzyl.nursing.domain.*;
import com.zzyl.nursing.dto.CheckInApplyDto;
import com.zzyl.nursing.dto.CheckInConfigDto;
import com.zzyl.nursing.dto.CheckInContractDto;
import com.zzyl.nursing.mapper.*;
import com.zzyl.nursing.service.ICheckInService;
import com.zzyl.nursing.vo.CheckInConfigVo;
import com.zzyl.nursing.vo.CheckInDetailVo;
import com.zzyl.nursing.vo.CheckInElderVo;
import com.zzyl.nursing.vo.ElderFamilyVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 入住Service业务层处理
 *
 * @author alexis
 * @date 2025-09-04
 */
@Service
public class CheckInServiceImpl extends ServiceImpl<CheckInMapper, CheckIn> implements ICheckInService {
    @Autowired
    private CheckInMapper checkInMapper;
    @Autowired
    private CheckInConfigMapper checkInConfigMapper;
    @Autowired
    private ElderMapper elderMapper;
    @Autowired
    private ContractMapper contractMapper;
    @Autowired
    private BedMapper bedMapper;

    /**
     * 查询入住
     *
     * @param id 入住主键
     * @return 入住
     */
    @Override
    public CheckIn selectCheckInById(Long id) {
        return checkInMapper.selectById(id);
    }

    /**
     * 查询入住列表
     *
     * @param checkIn 入住
     * @return 入住
     */
    @Override
    public List<CheckIn> selectCheckInList(CheckIn checkIn) {
        return checkInMapper.selectCheckInList(checkIn);
    }

    /**
     * 新增入住
     *
     * @param checkIn 入住
     * @return 结果
     */
    @Override
    public int insertCheckIn(CheckIn checkIn) {
        return checkInMapper.insert(checkIn);
    }

    /**
     * 修改入住
     *
     * @param checkIn 入住
     * @return 结果
     */
    @Override
    public int updateCheckIn(CheckIn checkIn) {
        return checkInMapper.updateById(checkIn);
    }

    /**
     * 批量删除入住
     *
     * @param ids 需要删除的入住主键
     * @return 结果
     */
    @Override
    public int deleteCheckInByIds(Long[] ids) {
        return checkInMapper.deleteBatchIds(Arrays.asList(ids));
    }

    /**
     * 删除入住信息
     *
     * @param id 入住主键
     * @return 结果
     */
    @Override
    public int deleteCheckInById(Long id) {
        return checkInMapper.deleteById(id);
    }

    /**
     * 入住申请
     *
     * @param checkInApplyDto 入住申请参数
     */
    @Transactional
    @Override
    public void applyCheckIn(CheckInApplyDto checkInApplyDto) {
        //1.检查老人是否已经入住
        Elder elderDB = elderMapper.selectOne(new LambdaQueryWrapper<Elder>()
                .eq(Elder::getIdCardNo, checkInApplyDto.getCheckInElderDto().getIdCardNo()));
        // 是，抛出异常，结束方法。  否，继续向下执行
        if (!ObjectUtils.isEmpty(elderDB) && elderDB.getStatus() != 5) {
            throw new ServiceException("该老人已入住，请勿重复入住！");
        }
        //2.更新床位状态为已入驻
        Bed bed = updateBedStatus(checkInApplyDto);

        //3.新增或更新老人
        Elder elder = insertOrUpdateElder(checkInApplyDto, bed, elderDB);

        //4.新增签约办理
        insertContract(checkInApplyDto, elder);

        //5.新增入住信息
        CheckIn checkIn = insertCheckIn(elder, checkInApplyDto, bed);

        //6.新增入住配置
        insertCheckInConfig(checkInApplyDto, checkIn);

    }

    /**
     * 根据ID查询入住详情信息
     *
     * @param id 入住记录ID
     * @return 入住详情VO对象
     */
    @Override
    public CheckInDetailVo selectCheckInDetailById(Long id) {
        CheckInDetailVo checkInDetailVo = new CheckInDetailVo();
        // 查询入住基本信息
        CheckIn checkIn = checkInMapper.selectById(id);

        //1.查询获取老人信息
        Elder elder = elderMapper.selectById(checkIn.getElderId());
        CheckInElderVo checkInElderVo = new CheckInElderVo();
        BeanUtils.copyBeanProp(checkInElderVo, elder);
        int age = IdCardNoUtils.getAgeByIdCard(elder.getIdCardNo());
        checkInElderVo.setAge(age);
        checkInDetailVo.setCheckInElderVo(checkInElderVo);

        //2.查询老人家人信息
        String remark = checkIn.getRemark();
        //JSON字符串 ===> List<ElderFamilyVo>
        List<ElderFamilyVo> list = JSONUtil.toList(remark, ElderFamilyVo.class);
        checkInDetailVo.setElderFamilyVoList(list);

        //3.查询入住配置信息
        CheckInConfig checkInConfig = checkInConfigMapper.selectOne(new LambdaQueryWrapper<CheckInConfig>()
                .eq(CheckInConfig::getCheckInId, checkIn.getId()));
        CheckInConfigVo checkInConfigVo = new CheckInConfigVo();
        BeanUtils.copyBeanProp(checkInConfigVo, checkInConfig);
        checkInConfigVo.setBedNumber(checkIn.getBedNumber());
        checkInConfigVo.setStartDate(checkIn.getStartDate());
        checkInConfigVo.setEndDate(checkIn.getEndDate());
        checkInDetailVo.setCheckInConfigVo(checkInConfigVo);

        //4.查询合同
        Contract contract = contractMapper.selectOne(new LambdaQueryWrapper<Contract>()
                .eq(Contract::getElderId, checkIn.getElderId()));
        checkInDetailVo.setContract(contract);

        return checkInDetailVo;
    }


    /**
     * 更新床位状态为已入住
     *
     * @param checkInApplyDto 入住申请信息，包含床位ID等配置信息
     * @return 更新后的床位信息
     * @throws ServiceException 当床位不存在或已被他人入住时抛出异常
     */
    private Bed updateBedStatus(CheckInApplyDto checkInApplyDto) {
        //查询有无此床位
        Long bedId = checkInApplyDto.getCheckInConfigDto().getBedId();
        Bed bed = bedMapper.selectById(bedId);
        if (ObjectUtils.isEmpty(bed) || bed.getBedStatus() == 1) {
            throw new ServiceException("床位不存在或已被他人入住，请重新选择1！");
        }
        bed.setBedStatus(1);
        bedMapper.updateById(bed);
        return bed;
    }


    /**
     * 插入入住配置信息
     *
     * @param checkInApplyDto 入住申请数据传输对象，包含配置相关信息
     * @param checkIn         入住信息对象，用于关联入住配置
     */
    private void insertCheckInConfig(CheckInApplyDto checkInApplyDto, CheckIn checkIn) {
        CheckInConfig checkInConfig = new CheckInConfig();
        BeanUtils.copyBeanProp(checkInConfig, checkInApplyDto.getCheckInConfigDto());
        checkInConfig.setCheckInId(checkIn.getId());
        checkInConfigMapper.insert(checkInConfig);
    }

    /**
     * 插入入住记录
     *
     * @param elder           老人信息对象
     * @param checkInApplyDto 入住申请数据传输对象
     * @return 入住记录对象
     */
    private CheckIn insertCheckIn(Elder elder, CheckInApplyDto checkInApplyDto, Bed bedDB) {
        // 获取入住配置信息
        CheckInConfigDto checkInConfigDto = checkInApplyDto.getCheckInConfigDto();

        // 创建入住记录对象并设置基本信息
        CheckIn checkIn = new CheckIn();
        checkIn.setElderName(elder.getName());
        checkIn.setElderId(elder.getId());
        checkIn.setIdCardNo(elder.getIdCardNo());
        checkIn.setStartDate(checkInConfigDto.getStartDate());
        checkIn.setEndDate(checkInConfigDto.getEndDate());
        checkIn.setNursingLevelName(checkInConfigDto.getNursingLevelName());

        checkIn.setBedNumber(bedDB.getBedNumber());

        // 设置入住状态为初始状态，并保存入住记录
        checkIn.setStatus(0);
        checkIn.setRemark(JSONUtil.toJsonStr(checkInApplyDto.getElderFamilyDtoList()));
        checkInMapper.insert(checkIn);
        return checkIn;
    }


    /**
     * 插入入住合同信息
     *
     * @param checkInApplyDto 入住申请DTO对象，包含合同相关信息
     * @param elder           入住老人对象，包含老人基本信息
     */
    private void insertContract(CheckInApplyDto checkInApplyDto, Elder elder) {
        CheckInContractDto checkInContractDto = checkInApplyDto.getCheckInContractDto();
        Contract contract = new Contract();
        BeanUtils.copyBeanProp(contract, checkInContractDto);
        //补全数据
        contract.setElderId(elder.getId());
        contract.setElderName(elder.getName());
        String contractNumber = "HT" + CodeGenerator.generateContractNumber();
        contract.setContractNumber(contractNumber);
        LocalDateTime startDate = checkInApplyDto.getCheckInConfigDto().getStartDate();
        LocalDateTime endDate = checkInApplyDto.getCheckInConfigDto().getEndDate();
        contract.setStartDate(startDate);
        contract.setEndDate(endDate);
        //根据合同开始时间设置合同状态：已开始为1，未开始为0
        Integer status = startDate.isBefore(LocalDateTime.now()) ? 1 : 0;
        contract.setStatus(status);
        contractMapper.insert(contract);
    }


    /**
     * 插入或更新老人信息
     *
     * @param checkInApplyDto 入住申请DTO，包含老人基本信息和床位配置信息
     * @param bed             床位信息对象
     * @param elderDB         数据库中已存在的老人信息，用于判断是更新还是插入操作
     * @return 更新或插入后的老人信息对象
     */
    private Elder insertOrUpdateElder(CheckInApplyDto checkInApplyDto, Bed bed, Elder elderDB) {
        Elder elder = new Elder();
        // 复制入住老人DTO中的属性到elder对象
        BeanUtils.copyBeanProp(elder, checkInApplyDto.getCheckInElderDto());
        // 设置床位ID和床位号
        elder.setBedId(checkInApplyDto.getCheckInConfigDto().getBedId());
        elder.setBedNumber(bed.getBedNumber());
        // 设置老人状态为1（在住）
        elder.setStatus(1);
        // 根据数据库中是否存在该老人信息来决定执行更新还是插入操作
        if (!ObjectUtils.isEmpty(elderDB)) {
            elder.setId(elderDB.getId());
            elderMapper.updateById(elder);
        } else {
            elderMapper.insert(elder);
        }
        return elder;
    }


}
