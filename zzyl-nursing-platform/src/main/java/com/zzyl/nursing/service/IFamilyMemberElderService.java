package com.zzyl.nursing.service;

import java.util.List;

import com.zzyl.nursing.domain.DevicePropertyStatusData;
import com.zzyl.nursing.domain.FamilyMemberElder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zzyl.nursing.dto.BindFamilyMemberRequestDto;
import com.zzyl.nursing.dto.MemberListDto;
import com.zzyl.nursing.vo.BindFamilyMemberVo;
import com.zzyl.nursing.vo.DeviceDataByDayOrWeekVo;
import com.zzyl.nursing.vo.FamilyElderVo;

/**
 * 老人-家属关联中间Service接口
 * 
 * @author alexis
 * @date 2025-09-20
 */
public interface IFamilyMemberElderService extends IService<FamilyMemberElder>
{

    /**
     * 新增老人-家属关联中间
     * 
     * @param bindFamilyMemberRequestDto 老人-家属关联中间
     * @return 结果
     */
    public int insertFamilyMemberElder(BindFamilyMemberRequestDto bindFamilyMemberRequestDto);

    List<BindFamilyMemberVo> selectAllFamilyMember();

    List<FamilyElderVo> selectFamilyMemberList(MemberListDto memberListDto);

    DevicePropertyStatusData selectDeviceInfoByiotId(String iotId);

    /**
     * 批量删除老人-家属关联中间
     *
     * @param id 需要删除的老人-家属关联中间主键集合
     * @return 结果
     */
    public int deleteFamilyMemberElderById(String id);

    List<DeviceDataByDayOrWeekVo> queryDeviceDataListByDay(String functionId, Long startTime, Long endTime, String iotId);

    List<DeviceDataByDayOrWeekVo> queryDeviceDataListByWeek(String functionId, Long startTime, Long endTime, String iotId);
}
