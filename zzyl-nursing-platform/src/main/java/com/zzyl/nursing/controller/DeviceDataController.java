package com.zzyl.nursing.controller;

import com.zzyl.common.annotation.Log;
import com.zzyl.common.core.controller.BaseController;
import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.common.core.domain.R;
import com.zzyl.common.core.page.TableDataInfo;
import com.zzyl.common.enums.BusinessType;
import com.zzyl.nursing.domain.DeviceData;
import com.zzyl.nursing.dto.DeviceDataPageReqDto;
import com.zzyl.nursing.service.IDeviceDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 设备数据表Controller
 *
 * @author alexis
 * @date 2025-09-15
 */
@Api(tags = "设备数据表管理")
@RestController
@RequestMapping("/nursing/data")
public class DeviceDataController extends BaseController {
    @Autowired
    private IDeviceDataService deviceDataService;

    /**
     * 查询设备数据列表
     */
    @PreAuthorize("@ss.hasPermi('elder:data:list')")
    @GetMapping("/list")
    @ApiOperation("查询设备数据列表")
    public TableDataInfo<DeviceData> list(@ApiParam("设备数据表查询参数") DeviceDataPageReqDto deviceDataPageReqDto) {
        return deviceDataService.selectDeviceDataList(deviceDataPageReqDto);
    }

    /**
     * 获取设备数据表详细信息
     */
    @ApiOperation("获取设备数据表详细信息")
    @PreAuthorize("@ss.hasPermi('nursing:deviceData:query')")
    @GetMapping(value = "/{id}")
    public R<DeviceData> getInfo(@ApiParam("设备数据表ID") @PathVariable("id") Long id) {
        return R.ok(deviceDataService.selectDeviceDataById(id));
    }

    /**
     * 新增设备数据表
     */
    @ApiOperation("新增设备数据表")
    @PreAuthorize("@ss.hasPermi('nursing:deviceData:add')")
    @Log(title = "设备数据表", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@ApiParam("设备数据表信息") @RequestBody DeviceData deviceData) {
        return toAjax(deviceDataService.insertDeviceData(deviceData));
    }

    /**
     * 修改设备数据表
     */
    @ApiOperation("修改设备数据表")
    @PreAuthorize("@ss.hasPermi('nursing:deviceData:edit')")
    @Log(title = "设备数据表", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@ApiParam("设备数据表信息") @RequestBody DeviceData deviceData) {
        return toAjax(deviceDataService.updateDeviceData(deviceData));
    }

    /**
     * 删除设备数据表
     */
    @ApiOperation("删除设备数据表")
    @PreAuthorize("@ss.hasPermi('nursing:deviceData:remove')")
    @Log(title = "设备数据表", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@ApiParam("设备数据表ID数组") @PathVariable Long[] ids) {
        return toAjax(deviceDataService.deleteDeviceDataByIds(ids));
    }
}
