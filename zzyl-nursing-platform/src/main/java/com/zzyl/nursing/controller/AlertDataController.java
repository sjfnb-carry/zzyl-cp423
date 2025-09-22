package com.zzyl.nursing.controller;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import com.zzyl.common.core.domain.R;
import com.zzyl.nursing.dto.AlertDataDto;
import com.zzyl.nursing.vo.AlertDataVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zzyl.common.annotation.Log;
import com.zzyl.common.core.controller.BaseController;
import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.common.enums.BusinessType;
import com.zzyl.nursing.domain.AlertData;
import com.zzyl.nursing.service.IAlertDataService;
import com.zzyl.common.utils.poi.ExcelUtil;
import com.zzyl.common.core.page.TableDataInfo;

/**
 * 报警数据Controller
 * 
 * @author alexis
 * @date 2025-09-20
 */
@Api(tags = "报警数据管理")
@RestController
@RequestMapping("/nursing/alertData")
public class AlertDataController extends BaseController
{
    @Autowired
    private IAlertDataService alertDataService;

    /**
     * 查询报警数据列表
     */
    @ApiOperation("查询报警数据列表")
    @GetMapping("/list")
    public TableDataInfo<AlertDataVo> list(AlertDataDto alertDataDto)
    {
        return alertDataService.selectAlertDataList(alertDataDto);
    }


    /**
     * 获取报警数据详细信息
     */
    @ApiOperation("获取报警数据详细信息")
    @GetMapping(value = "/{id}")
    public R<AlertData> getInfo(@ApiParam("报警数据ID") @PathVariable("id") Long id)
    {
        return R.ok(alertDataService.selectAlertDataById(id));
    }

    /**
     * 新增报警数据
     */
    @ApiOperation("新增报警数据")
    @Log(title = "报警数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@ApiParam("报警数据信息") @RequestBody AlertData alertData)
    {
        return toAjax(alertDataService.insertAlertData(alertData));
    }

    /**
     * 删除报警数据
     */
    @ApiOperation("删除报警数据")
    @Log(title = "报警数据", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@ApiParam("报警数据ID数组") @PathVariable Long[] ids)
    {
        return toAjax(alertDataService.deleteAlertDataByIds(ids));
    }

    @ApiOperation("处理设备报警数据")
    @PutMapping("/handleAlertData")
    public R<Integer> handleAlertData(@ApiParam("报警数据信息") @RequestBody Map<String,Object> params)
    {
        return R.ok(alertDataService.updateAlertData(params));
    }
}
