package com.zzyl.nursing.controller.member;

import com.zzyl.common.core.controller.BaseController;
import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.nursing.dto.DeviceDataQueryDto;
import com.zzyl.nursing.dto.UserLoginRequestDto;
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


    @PostMapping("/login")
    @ApiOperation("小程序登录")
    public AjaxResult login(@RequestBody UserLoginRequestDto userLoginRequestDto) {
        LoginVo loginVo = familyMemberService.login(userLoginRequestDto);
        return success(loginVo);
    }

    @PostMapping("/add")
    @ApiOperation("绑定老人")
    public AjaxResult bindElder(@RequestBody Map<String, Object> params) {
        familyMemberService.bindElder(params);
        return success();
    }

    @GetMapping("/my")
    public AjaxResult listElders() {
        List<Map<String, Object>> elders = familyMemberService.listElders();
        return success(elders);
    }

    @GetMapping("/list-by-page")
    public AjaxResult listByPage(@PathParam("pageNum") Integer pageNum, @PathParam("pageSize") Integer pageSize) {
        List<ElderInfoVo> elders = familyMemberService.listByPage(pageNum, pageSize);
        return success(elders);
    }

    @GetMapping("/queryServiceProperties/{iotId}")
    public AjaxResult queryDevicePropertyStatus(@PathVariable("iotId") String iotId) {
        AjaxResult map = familyMemberService.queryDevicePropertyStatus(iotId);
        return map;
    }


    @GetMapping("/queryDeviceDataListByDay")
    public AjaxResult queryDeviceDataListByDay(DeviceDataQueryDto dto) {
        List<Map<String, Object>> detailList = familyMemberService.queryDeviceDataListByDay(dto);
        return success(detailList);
    }

    @GetMapping("/queryDeviceDataListByWeek")
    public AjaxResult queryDeviceDataListByWeek(DeviceDataQueryDto dto) {
        List<Map<String, Object>> detailList = familyMemberService.queryDeviceDataListByWeek(dto);
        return success(detailList);
    }


}