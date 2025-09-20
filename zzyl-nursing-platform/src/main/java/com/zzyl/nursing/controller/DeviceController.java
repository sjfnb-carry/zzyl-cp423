package com.zzyl.nursing.controller;

import com.huaweicloud.sdk.iotda.v5.model.ServiceCapability;
import com.zzyl.common.core.controller.BaseController;
import com.zzyl.common.core.domain.R;
import com.zzyl.common.core.page.TableDataInfo;
import com.zzyl.nursing.domain.Device;
import com.zzyl.nursing.dto.DeviceDto;
import com.zzyl.nursing.service.IDeviceService;
import com.zzyl.nursing.vo.DeviceDetailVo;
import com.zzyl.nursing.vo.ProductVo;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 设备表Controller
 *
 * @author alexis
 * @date 2025-09-14
 */
@Api(tags = "设备表管理")
@RestController
@RequestMapping("/nursing/device")
public class DeviceController extends BaseController {
    @Autowired
    private IDeviceService deviceService;

    /**
     * 查询设备表列表
     */
    @ApiOperation("查询设备表列表")
    @PreAuthorize("@ss.hasPermi('nursing:device:list')")
    @GetMapping("/list")
    public TableDataInfo<List<Device>> list(@ApiParam("设备表查询条件") Device device) {
        startPage();
        List<Device> list = deviceService.selectDeviceList(device);
        return getDataTable(list);
    }

    /**
     * 物联网平台同步产品列表
     *
     * @return
     */
    @ApiOperation("同步产品列表")
    @PreAuthorize("@ss.hasPermi('nursing:device:query')")
    @PostMapping("/syncProductList")
    public R<String> syncProductList() {
        deviceService.syncProductList();
        return R.ok();
    }

    /**
     * 获取所有产品信息
     *
     * @return R<List<ProductVo>> 统一响应结果，包含产品信息列表
     */
    @GetMapping("/allProduct")
    @PreAuthorize("@ss.hasPermi('nursing:device:query')")
    public R<List<ProductVo>> allProduct() {
        // 调用设备服务获取所有产品信息
        List<ProductVo> list = deviceService.allProduct();
        // 返回成功响应结果
        return R.ok(list);
    }

    /**
     * 注册设备
     *
     * @param dto
     * @return
     */
    @ApiOperation("设备注册")
    @PostMapping("/register")
    public R<String> register(@RequestBody DeviceDto dto) {
        deviceService.register(dto);
        return R.ok();
    }

    /**
     * 获取设备详细信息
     */
    @GetMapping("/{iotId}")
    @ApiOperation("获取设备详细信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iotId", value = "物联网设备id", required = true, dataTypeClass = String.class)
    })
    public R<DeviceDetailVo> getInfo(@PathVariable("iotId") String iotId) {
        DeviceDetailVo detailVo = deviceService.getDeviceDetail(iotId);
        return R.ok(detailVo);
    }

    /**
     * 查询设备上报数据
     */
    @GetMapping("/queryServiceProperties/{iotId}")
    @ApiOperation("查询设备上报数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iotId", value = "物联网设备id", required = true, dataTypeClass = String.class)
    })
    public R<List<Map<String, Object>>> queryServiceProperties(@PathVariable("iotId") String iotId) {
        List<Map<String, Object>> list = deviceService.queryServiceProperties(iotId);
        return R.ok(list);
    }

    @PutMapping
    @ApiOperation("更新设备信息")
    public R<String> updateDevice(@RequestBody DeviceDto dto) {
        deviceService.updateDevice(dto);
        return R.ok();
    }

    @DeleteMapping("{iotId}")
    @ApiOperation("删除设备")
    public R<String> deleteDevice(@PathVariable("iotId") String iotId) {
        deviceService.deleteDeviceByIotId(iotId);
        return R.ok();
    }


    /**
     * 查询设备产品详情
     * @param productKey 产品key，用于标识特定的产品
     * @return 返回包含服务功能列表的结果对象
     */
    @GetMapping("/queryProduct/{productKey}")
    @ApiOperation("查询设备产品详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "productKey", value = "产品key", required = true, dataTypeClass = String.class)
    })
    public R<List<ServiceCapability>> queryProduct(@PathVariable("productKey") String productKey) {
        // 调用设备服务查询指定产品key的产品信息
        List<ServiceCapability> serviceCapabilities = deviceService.queryProduct(productKey);
        return R.ok(serviceCapabilities);
    }

}
