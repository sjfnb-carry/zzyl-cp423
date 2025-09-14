package com.zzyl.nursing.controller.member;

import com.zzyl.common.core.controller.BaseController;
import com.zzyl.common.core.domain.R;
import com.zzyl.common.core.page.TableDataInfo;
import com.zzyl.nursing.domain.NursingProject;
import com.zzyl.nursing.service.INursingProjectService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/member")
public class MemberNursingController extends BaseController {
    @Autowired
    INursingProjectService nursingProjectService;

    @GetMapping("/orders/project/page")
    @ApiOperation("查询项目列表")
    public TableDataInfo<List<NursingProject>> list(@ApiParam("护理项目查询条件") NursingProject nursingProject) {
        startPage();
        List<NursingProject> list = nursingProjectService.selectNursingProjectList(nursingProject);
        return getDataTable(list);
    }

    /**
     * 获取护理项目详细信息
     */
    @ApiOperation("获取护理项目详细信息")
    @GetMapping("/orders/project/{id}")
    public R<NursingProject> getInfo(@ApiParam("护理项目ID") @PathVariable("id") Long id) {
        return R.ok(nursingProjectService.getById(id));
    }
}
