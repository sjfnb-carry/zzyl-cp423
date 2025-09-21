package com.zzyl.nursing.controller;

import com.zzyl.common.core.controller.BaseController;
import com.zzyl.common.core.domain.R;
import com.zzyl.common.core.page.TableDataInfo;
import com.zzyl.nursing.domain.NursingTask;
import com.zzyl.nursing.dto.NursingTaskDto;
import com.zzyl.nursing.dto.NursingTaskExecutionDto;
import com.zzyl.nursing.service.INursingTaskService;
import com.zzyl.nursing.vo.NursingTaskDetailVo;
import com.zzyl.nursing.vo.NursingTaskVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 护理任务Controller
 *
 * @author alexis
 * @date 2024-11-17
 */
@RestController
@RequestMapping("/nursing/nursingTask")
@Api(tags = "护理任务管理")
public class NursingTaskController extends BaseController {
    @Autowired
    private INursingTaskService nursingTaskService;

    /**
     * 查询护理任务列表
     */
    @GetMapping("/list")
    @ApiOperation(value = "获取护理任务列表")
    public TableDataInfo<List<NursingTask>> list(@ApiParam(value = "护理任务查询条件") NursingTaskDto nursingTaskDto) {
        startPage();
        List<NursingTaskVo> list = nursingTaskService.selectNursingTaskList(nursingTaskDto);
        return getDataTable(list);
    }


    /**
     * 获取护理任务详细信息
     */
    @GetMapping(value = "/{id}")
    @ApiOperation("获取护理任务详细信息")
    public R<NursingTaskDetailVo> getInfo(@ApiParam(value = "护理任务ID", required = true) @PathVariable("id") Long id) {
        return R.ok(nursingTaskService.selectNursingTaskById(id));
    }

    /**
     * 取消护理任务
     *
     * @param params 请求参数Map，包含取消护理任务所需的相关参数
     * @return R<String> 统一返回结果，成功时返回状态码200和空字符串数据
     */
    @PutMapping("/cancel")
    @ApiOperation("取消护理任务")
    public R<String> cancel(@RequestBody Map<String, Object> params) {
        // 调用护理任务服务执行取消操作
        nursingTaskService.cancel(params);
        return R.ok();
    }


    /**
     * 执行护理任务
     *
     * @param dto 护理任务执行数据传输对象，包含任务执行的相关信息
     * @return R<String> 响应结果对象，成功时返回状态码200和空字符串数据
     */
    @PutMapping("/do")
    @ApiOperation("执行护理任务")
    public R<String> doTask(@RequestBody NursingTaskExecutionDto dto) {
        // 调用护理任务服务执行具体的任务处理逻辑
        nursingTaskService.doTask(dto);
        return R.ok();
    }

    @PutMapping("/updateTime")
    @ApiOperation("更新护理任务执行时间")
    public R<String> updateTime(@RequestBody Map<String, Object> params) {
        // 调用护理任务服务执行具体的任务处理逻辑
        nursingTaskService.updateTime(params);
        return R.ok();
    }


}


