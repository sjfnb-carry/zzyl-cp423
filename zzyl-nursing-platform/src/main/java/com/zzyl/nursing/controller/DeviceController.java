package com.zzyl.nursing.controller;

import com.zzyl.common.core.controller.BaseController;
import com.zzyl.common.core.domain.R;
import com.zzyl.common.core.page.TableDataInfo;
import com.zzyl.nursing.domain.Device;
import com.zzyl.nursing.dto.DeviceDto;
import com.zzyl.nursing.service.IDeviceService;
import com.zzyl.nursing.vo.ProductVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * @param dto
     * @return
     */
    @ApiOperation("设备注册")
    @PostMapping("/register")
    public R<String> register(@RequestBody DeviceDto dto){
        deviceService.register(dto);
        return R.ok();
    }
}
