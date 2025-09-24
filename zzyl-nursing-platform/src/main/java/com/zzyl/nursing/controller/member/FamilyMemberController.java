package com.zzyl.nursing.controller.member;

import com.zzyl.common.core.controller.BaseController;
import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.nursing.dto.DeviceDataQueryDto;
import com.zzyl.nursing.dto.UserLoginRequestDto;
import com.zzyl.nursing.service.IFamilyMemberElderService;
import com.zzyl.nursing.service.IFamilyMemberService;
import com.zzyl.nursing.vo.ElderInfoVo;
import com.zzyl.nursing.vo.LoginVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.List;
import java.util.Map;

/**
 * 老人家属Controller
 *
 * @author ruoyi
 * @date 2024-09-02
 */
@RestController
@RequestMapping("/member/user")
@Api(tags = "老人家属相关接口")
public class FamilyMemberController extends BaseController {
    @Autowired
    private IFamilyMemberService familyMemberService;
    @Autowired
    private IFamilyMemberElderService familyMemberElderService;


    /**
     * 小程序登录接口
     *
     * @param userLoginRequestDto 用户登录请求参数
     * @return 登录结果，包含token等信息
     */
    @PostMapping("/login")
    @ApiOperation("小程序登录")
    public AjaxResult login(@RequestBody UserLoginRequestDto userLoginRequestDto) {
        LoginVo loginVo = familyMemberService.login(userLoginRequestDto);
        return success(loginVo);
    }

    /**
     * 绑定老人接口
     *
     * @param params 绑定参数，包含老人和家属信息
     * @return 操作结果
     */
    @PostMapping("/add")
    @ApiOperation("绑定老人")
    public AjaxResult bindElder(@RequestBody Map<String, Object> params) {
        familyMemberService.bindElder(params);
        return success();
    }

    /**
     * 查询当前用户绑定的老人列表
     *
     * @return 老人信息列表
     */
    @GetMapping("/my")
    public AjaxResult listElders() {
        List<Map<String, Object>> elders = familyMemberService.listElders();
        return success(elders);
    }

    /**
     * 分页查询老人信息列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页后的老人信息列表
     */
    @GetMapping("/list-by-page")
    public AjaxResult listByPage(@PathParam("pageNum") Integer pageNum, @PathParam("pageSize") Integer pageSize) {
        List<ElderInfoVo> elders = familyMemberService.listByPage(pageNum, pageSize);
        return success(elders);
    }

    /**
     * 查询设备属性状态
     *
     * @param iotId 设备ID
     * @return 设备属性状态信息
     */
    @GetMapping("/queryServiceProperties/{iotId}")
    public AjaxResult queryDevicePropertyStatus(@PathVariable("iotId") String iotId) {
        AjaxResult map = familyMemberService.queryDevicePropertyStatus(iotId);
        return map;
    }

    /**
     * 按天查询设备数据列表
     *
     * @param dto 设备数据查询参数
     * @return 设备数据列表
     */
    @GetMapping("/queryDeviceDataListByDay")
    public AjaxResult queryDeviceDataListByDay(DeviceDataQueryDto dto) {
        List<Map<String, Object>> detailList = familyMemberService.queryDeviceDataListByDay(dto);
        return success(detailList);
    }

    /**
     * 按周查询设备数据列表
     *
     * @param dto 设备数据查询参数
     * @return 设备数据列表
     */
    @GetMapping("/queryDeviceDataListByWeek")
    public AjaxResult queryDeviceDataListByWeek(DeviceDataQueryDto dto) {
        List<Map<String, Object>> detailList = familyMemberService.queryDeviceDataListByWeek(dto);
        return success(detailList);
    }

    @DeleteMapping("/deleteById")
    public AjaxResult delete(Long id) {
        familyMemberElderService.removeById(id);
        return success();
    }

}