package com.zzyl.nursing.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzyl.nursing.domain.Elder;
import com.zzyl.nursing.service.IElderService;
import com.zzyl.nursing.service.INursingTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 定时生成月度护理任务
 */
@Component
@Slf4j
public class GenerateNursingTaskJob {
    @Autowired
    private IElderService elderService;

    @Autowired
    private INursingTaskService nursingTaskService;

    public void generateNursingTask() {
        // 查询所有老人
        List<Elder> elderList = elderService.list(Wrappers.<Elder>lambdaQuery().eq(Elder::getStatus, 1));
        elderList.forEach(elder -> nursingTaskService.generateMonthlyTask(elder));
        log.info("生成月任务成功");
    }
}
