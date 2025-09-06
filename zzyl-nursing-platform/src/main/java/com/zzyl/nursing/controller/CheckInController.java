package com.zzyl.nursing.controller;

import com.zzyl.common.annotation.Log;
import com.zzyl.common.core.controller.BaseController;
import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.common.core.domain.R;
import com.zzyl.common.core.page.TableDataInfo;
import com.zzyl.common.enums.BusinessType;
import com.zzyl.common.utils.poi.ExcelUtil;
import com.zzyl.nursing.domain.CheckIn;
import com.zzyl.nursing.dto.CheckInApplyDto;
import com.zzyl.nursing.service.ICheckInService;
import com.zzyl.nursing.vo.CheckInDetailVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 入住Controller
 *
 * @author alexis
 * @date 2025-09-04
 */
@Api(tags = "入住管理")
@RestController
@RequestMapping("/nursing/checkIn")
public class CheckInController extends BaseController {
    @Autowired
    private ICheckInService checkInService;

    /**
     * 查询入住列表
     */
    @ApiOperation("查询入住列表")
    @PreAuthorize("@ss.hasPermi('nursing:checkIn:list')")
    @GetMapping("/list")
    public TableDataInfo<List<CheckIn>> list(@ApiParam("入住查询条件") CheckIn checkIn) {
        startPage();
        List<CheckIn> list = checkInService.selectCheckInList(checkIn);
        return getDataTable(list);
    }

    /**
     * 导出入住列表
     */
    @ApiOperation("导出入住列表")
    @PreAuthorize("@ss.hasPermi('nursing:checkIn:export')")
    @Log(title = "入住", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(@ApiParam(value = "入住查询条件") HttpServletResponse response, CheckIn checkIn) {
        List<CheckIn> list = checkInService.selectCheckInList(checkIn);
        ExcelUtil<CheckIn> util = new ExcelUtil<CheckIn>(CheckIn.class);
        util.exportExcel(response, list, "入住数据");
    }

    /**
     * 获取入住详细信息
     */
    @ApiOperation("获取入住详细信息")
    @PreAuthorize("@ss.hasPermi('nursing:checkIn:query')")
    @GetMapping(value = "/{id}")
    public R<CheckIn> getInfo(@ApiParam("入住ID") @PathVariable("id") Long id) {
        return R.ok(checkInService.selectCheckInById(id));
    }

    /**
     * 新增入住
     */
    @ApiOperation("新增入住")
    @PreAuthorize("@ss.hasPermi('nursing:checkIn:add')")
    @Log(title = "入住", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@ApiParam("入住信息") @RequestBody CheckIn checkIn) {
        return toAjax(checkInService.insertCheckIn(checkIn));
    }

    /**
     * 修改入住
     */
    @ApiOperation("修改入住")
    @PreAuthorize("@ss.hasPermi('nursing:checkIn:edit')")
    @Log(title = "入住", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@ApiParam("入住信息") @RequestBody CheckIn checkIn) {
        return toAjax(checkInService.updateCheckIn(checkIn));
    }

    /**
     * 删除入住
     */
    @ApiOperation("删除入住")
    @PreAuthorize("@ss.hasPermi('nursing:checkIn:remove')")
    @Log(title = "入住", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@ApiParam("入住ID数组") @PathVariable Long[] ids) {
        return toAjax(checkInService.deleteCheckInByIds(ids));
    }

    /**
     * 入住办理
     */
    @ApiOperation("入住办理")
    @PreAuthorize("@ss.hasPermi('nursing:checkIn:add')")
    @PostMapping("/apply")
    public R<String> apply(@ApiParam("申请入住信息") @RequestBody CheckInApplyDto checkInApplyDto) {
        checkInService.applyCheckIn(checkInApplyDto);
        return R.ok();
    }

    /**
     * 获取入住详细信息
     */
    @ApiOperation("获取入住详细信息")
    @PreAuthorize("@ss.hasPermi('nursing:checkIn:query')")
    @GetMapping(value = "/detail/{id}")
    public R<CheckInDetailVo> getDetail(@ApiParam("入住ID") @PathVariable("id") Long id) {
        return R.ok(checkInService.selectCheckInDetailById(id));
    }

}
