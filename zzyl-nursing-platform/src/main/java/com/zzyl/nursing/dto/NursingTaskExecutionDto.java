
package com.zzyl.nursing.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 护理任务执行信息 DTO
 */
@Data
@ApiModel(value = "护理任务执行信息")
public class NursingTaskExecutionDto {

    /**
     * 护理任务ID
     */
    @ApiModelProperty(value = "护理任务ID")
    private Long taskId;

    /**
     * 预计服务时间（执行时间）
     */
    @ApiModelProperty(value = "执行时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedServerTime;

    /**
     * 执行记录
     */
    @ApiModelProperty(value = "执行记录")
    private String mark;

    /**
     * 任务图片
     */
    @ApiModelProperty(value = "任务图片")
    private String taskImage;
}