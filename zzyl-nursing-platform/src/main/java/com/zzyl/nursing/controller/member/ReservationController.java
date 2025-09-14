package com.zzyl.nursing.controller.member;

import com.zzyl.common.annotation.Log;
import com.zzyl.common.core.controller.BaseController;
import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.common.core.domain.R;
import com.zzyl.common.enums.BusinessType;
import com.zzyl.common.utils.poi.ExcelUtil;
import com.zzyl.nursing.domain.Reservation;
import com.zzyl.nursing.dto.ReservationDto;
import com.zzyl.nursing.service.IReservationService;
import com.zzyl.nursing.vo.ReservationVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 预约信息Controller
 *
 * @author alexis
 * @date 2025-09-13
 */
@Api(tags = "预约信息管理")
@RestController
@RequestMapping("/member/reservation")
public class ReservationController extends BaseController {
    @Autowired
    private IReservationService reservationService;

    /**
     * 查询预约信息列表
     */
    @ApiOperation("查询预约信息列表")
    @GetMapping("/page")
    public AjaxResult list(@ApiParam("预约信息查询条件") Reservation reservation) {
        startPage();
        List<Reservation> list = reservationService.selectReservationList(reservation);
        return AjaxResult.success(getDataTable(list));
    }

    /**
     * 导出预约信息列表
     */
    @ApiOperation("导出预约信息列表")
    @Log(title = "预约信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(@ApiParam(value = "预约信息查询条件") HttpServletResponse response, Reservation reservation) {
        List<Reservation> list = reservationService.selectReservationList(reservation);
        ExcelUtil<Reservation> util = new ExcelUtil<Reservation>(Reservation.class);
        util.exportExcel(response, list, "预约信息数据");
    }

    /**
     * 获取预约信息详细信息
     */
    @ApiOperation("获取预约信息详细信息")
    @GetMapping(value = "/{id}")
    public R<Reservation> getInfo(@ApiParam("预约信息ID") @PathVariable("id") Long id) {
        return R.ok(reservationService.selectReservationById(id));
    }

    /**
     * 新增预约信息
     */
    @ApiOperation("新增预约信息")
    @Log(title = "预约信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@ApiParam("预约信息信息") @RequestBody ReservationDto reservationDto) {
        return toAjax(reservationService.insertReservation(reservationDto));
    }

    /**
     * 修改预约信息
     */
    @ApiOperation("修改预约信息")
    @Log(title = "预约信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@ApiParam("预约信息信息") @RequestBody Reservation reservation) {
        return toAjax(reservationService.updateReservation(reservation));
    }

    /**
     * 删除预约信息
     */
    @ApiOperation("删除预约信息")
    @Log(title = "预约信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@ApiParam("预约信息ID数组") @PathVariable Long[] ids) {
        return toAjax(reservationService.deleteReservationByIds(ids));
    }

    //查询当天取消预约数量
    @GetMapping("/cancelled-count")
    public AjaxResult searchCancelledCount() {
        Integer data = reservationService.searchCancelledCount();
        return AjaxResult.success(data);
    }

    //查询每个时间段剩余预约次数
    @GetMapping("/countByTime")
    public AjaxResult countByTime(Long time) {
        List<ReservationVo> list = reservationService.countByTime(time);
        return AjaxResult.success(list);
    }

    //取消预约
    @PutMapping("/{id}/cancel")
    public AjaxResult cancelReservation(@ApiParam("预约信息ID") @PathVariable("id") Long id) {
        Integer i = reservationService.cancelReservation(id);
        return toAjax(i);
    }


}
