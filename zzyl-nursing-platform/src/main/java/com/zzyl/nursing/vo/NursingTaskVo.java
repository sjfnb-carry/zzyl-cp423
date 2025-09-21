package com.zzyl.nursing.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel("护理任务VO")
public class NursingTaskVo {

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("护理员id")
    private String nursingId;

    @ApiModelProperty("项目id")
    private Integer projectId;

    @ApiModelProperty("护理项目名称")
    private String projectName;

    @ApiModelProperty("老人id")
    private Long elderId;

    @ApiModelProperty("老人姓名")
    private String elderName;

    @ApiModelProperty("床位编号")
    private String bedNumber;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("预计服务时间")
    private LocalDateTime estimatedServerTime;

    @ApiModelProperty("状态 1待执行 2已执行 3已关闭")
    private Integer status;

    @ApiModelProperty("护理员姓名列表")
    private List<String> nursingName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
