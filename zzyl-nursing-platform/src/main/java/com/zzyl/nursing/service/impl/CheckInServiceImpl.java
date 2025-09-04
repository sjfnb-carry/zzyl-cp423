package com.zzyl.nursing.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzyl.common.exception.base.BaseException;
import com.zzyl.common.utils.CodeGenerator;
import com.zzyl.common.utils.bean.BeanUtils;
import com.zzyl.nursing.domain.Bed;
import com.zzyl.nursing.domain.CheckIn;
import com.zzyl.nursing.domain.Contract;
import com.zzyl.nursing.domain.Elder;
import com.zzyl.nursing.dto.CheckInApplyDto;
import com.zzyl.nursing.dto.CheckInElderDto;
import com.zzyl.nursing.dto.ElderFamilyDto;
import com.zzyl.nursing.mapper.*;
import com.zzyl.nursing.service.ICheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    @Override
    public void apply(CheckInApplyDto checkInApplyDto) {
        //1.判断老人是否已入住
        String idCardNo = checkInApplyDto.getCheckInElderDto().getIdCardNo();
        LambdaQueryWrapper<Elder> qw = new LambdaQueryWrapper<>();
        qw.eq(Elder::getIdCardNo, idCardNo)
                .eq(Elder::getStatus, 1);
        Elder elder = elderMapper.selectOne(qw);
        if (!ObjectUtils.isEmpty(elder)) {
            throw new BaseException("该老人已入住");
        }
        //2.更新床位状态
        Bed bed = bedMapper.selectById(checkInApplyDto.getCheckInConfigDto().getBedId());
        bed.setBedStatus(1);
        bedMapper.updateById(bed);

        //3.新增或更新老人
        elder = insertOrUpdateElder(checkInApplyDto.getCheckInElderDto(), checkInApplyDto.getElderFamilyDtoList(), bed);

        //4.新增签约办理
        //生成合同编号
        String contractNo = "HT" + CodeGenerator.generateContractNumber();
        insertContract(checkInApplyDto, elder, contractNo);
    }

    private void insertContract(CheckInApplyDto checkInApplyDto, Elder elder, String contractNo) {
        Contract contract = new Contract();
        BeanUtils.copyBeanProp(contract, checkInApplyDto.getCheckInContractDto());
        contract.setElderId(elder.getId());
        contract.setElderName(elder.getName());
        contract.setContractNumber(contractNo);
        LocalDateTime startDate = checkInApplyDto.getCheckInConfigDto().getStartDate();
        LocalDateTime endDate = checkInApplyDto.getCheckInConfigDto().getEndDate();
        int status = endDate.isBefore(LocalDateTime.now()) ? 0 : 1;
        contract.setStatus(status);
        contract.setStartDate(startDate);

    }

    private Elder insertOrUpdateElder(CheckInElderDto checkInElderDto, List<ElderFamilyDto> elderFamilyDtoList, Bed bed) {
        Elder elder = new Elder();
        BeanUtils.copyBeanProp(elder, checkInElderDto);
        elder.setBedId(bed.getId());
        elder.setBedNumber(bed.getBedNumber());
        elder.setRemark(JSONUtil.toJsonStr(elderFamilyDtoList));
        // 查询老人信息，（身份证号、状态不为1）
        LambdaQueryWrapper<Elder> qw = new LambdaQueryWrapper<>();
        qw.eq(Elder::getIdCardNo, checkInElderDto.getIdCardNo()).ne(Elder::getStatus, 1);
        Elder elderdb = elderMapper.selectOne(qw);
        //如果存在就修改，不存在就新增
        if (!ObjectUtils.isEmpty(elderdb)) {
            elder.setId(elderdb.getId());
            elderMapper.updateById(elder);
        } else {
            elderMapper.insert(elder);
        }
        return elder;
    }


}
