package com.zzyl.nursing.dto;

import com.zzyl.nursing.domain.NursingTask;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NursingTaskDto extends NursingTask {

    @ApiModelProperty("老人姓名")
    private String elderName;

    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty("护理员ID")
    private Long nurseId;

    @ApiModelProperty("页码")
    private Integer pageNum;

    @ApiModelProperty("每页显示条数")
    private Integer pageSize;

    @ApiModelProperty("护理项目ID")
    private Integer projectId;

    @ApiModelProperty("开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("状态 (1待执行 2已执行 3已关闭)")
    private Integer status;
}