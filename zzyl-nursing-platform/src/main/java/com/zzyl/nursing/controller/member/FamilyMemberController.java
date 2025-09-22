package com.zzyl.nursing.controller.member;

import com.zzyl.common.annotation.Log;
import com.zzyl.common.core.controller.BaseController;
import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.common.core.domain.R;
import com.zzyl.common.enums.BusinessType;
import com.zzyl.nursing.domain.DevicePropertyStatusData;
import com.zzyl.nursing.dto.BindFamilyMemberRequestDto;
import com.zzyl.nursing.dto.DeviceDto;
import com.zzyl.nursing.dto.MemberListDto;
import com.zzyl.nursing.dto.UserLoginRequestDto;
import com.zzyl.nursing.service.IFamilyMemberElderService;
import com.zzyl.nursing.service.IFamilyMemberService;
import com.zzyl.nursing.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 老人家属Controller
 *
 * @author ruoyi
 * @date 2024-09-02
 */
@Slf4j
@RestController
@RequestMapping("/member/user")
@Api(tags = "老人家属相关接口")
public class FamilyMemberController extends BaseController {
    @Autowired
    private IFamilyMemberService familyMemberService;
    @Autowired
    private IFamilyMemberElderService familyMemberElderService;


    @PostMapping("/login")
    @ApiOperation("小程序登录")
    public AjaxResult login(@RequestBody UserLoginRequestDto userLoginRequestDto) {
        LoginVo loginVo = familyMemberService.login(userLoginRequestDto);
        return success(loginVo);
    }

    /**
     * 新增老人家属
     */
    @ApiOperation("新增老人家属")
    @Log(title = "老人家属", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult add(@ApiParam("老人家属信息") @RequestBody BindFamilyMemberRequestDto bindFamilyMemberRequestDto) {
        int i = familyMemberElderService.insertFamilyMemberElder(bindFamilyMemberRequestDto);
        if (i == 0) {
            return error("绑定失败,请重试");
        }
        return toAjax(i);
    }

    @ApiOperation("查询当前登录用户的所有老人")
    @GetMapping("/my")
    public R<List<BindFamilyMemberVo>> selectAllFamilyMember() {
        return R.ok(familyMemberElderService.selectAllFamilyMember());
    }

    /**
     * 分页查询老人家属列表
     */
    @ApiOperation("分页查询老人家属列表")
    @GetMapping("/list-by-page")
    public AjaxResult listByPage(MemberListDto memberListDto) {
        List<FamilyElderVo> list = familyMemberElderService.selectFamilyMemberList(memberListDto);
        return  AjaxResult.success(list);
    }

    @GetMapping("/queryServiceProperties/{iotId}")
    public AjaxResult selectDevice(@PathVariable String iotId){
        DevicePropertyStatusData deviceData = familyMemberElderService.selectDeviceInfoByiotId(iotId);
        if (deviceData == null) {
            return AjaxResult.error("设备不存在");
        }
    return AjaxResult.success(deviceData);

    }
    @PostMapping("/QueryDevicePropertyStatus")
    public AjaxResult queryDevicePropertyStatus(@RequestBody DeviceDto deviceDto){
        log.info("QueryDevicePropertyStatus查询设备属性{}", deviceDto);
        return null;
    }
    /**
     * 删除老人-家属关联中间
     */
    @ApiOperation("删除老人-家属关联中间")
    @Log(title = "老人-家属关联中间", businessType = BusinessType.DELETE)
    @DeleteMapping("/deleteById")
    public AjaxResult remove(@ApiParam("老人-家属关联中间ID") @RequestParam("id") String id)
    {
        return toAjax(familyMemberElderService.deleteFamilyMemberElderById(id));
    }

    @GetMapping("/queryDeviceDataListByDay")
    @ApiOperation("按天统计查询指标数据")
    public AjaxResult queryDeviceDataListByDay(
            @ApiParam("功能ID") @RequestParam("functionId") String functionId,
            @ApiParam("开始时间") @RequestParam("startTime") Long startTime,
            @ApiParam("结束时间") @RequestParam("endTime") Long endTime,
            @ApiParam("设备ID") @RequestParam("iotId") String iotId) {
        List<DeviceDataByDayOrWeekVo> result = familyMemberElderService.queryDeviceDataListByDay(functionId, startTime, endTime, iotId);
        return AjaxResult.success(result);
    }

    @GetMapping("/queryDeviceDataListByWeek")
    @ApiOperation("按天统计查询指标数据")
    public AjaxResult queryDeviceDataListByWeek(
            @ApiParam("功能ID") @RequestParam("functionId") String functionId,
            @ApiParam("开始时间") @RequestParam("startTime") Long startTime,
            @ApiParam("结束时间") @RequestParam("endTime") Long endTime,
            @ApiParam("设备ID") @RequestParam("iotId") String iotId) {
        List<DeviceDataByDayOrWeekVo> result = familyMemberElderService.queryDeviceDataListByWeek(functionId, startTime, endTime, iotId);
        return AjaxResult.success(result);

    }
}