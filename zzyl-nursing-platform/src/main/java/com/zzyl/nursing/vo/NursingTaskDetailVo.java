package com.zzyl.nursing.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 护理任务VO对象
 */
@Data
@ApiModel(value = "护理任务VO")
public class NursingTaskDetailVo {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "护理员id")
    private String nursingId;

    @ApiModelProperty(value = "护理员姓名列表")
    private List<String> nursingName;

    @ApiModelProperty(value = "项目id")
    private Long projectId;

    @ApiModelProperty(value = "护理项目名称")
    private String projectName;

    @ApiModelProperty(value = "老人id")
    private Long elderId;

    @ApiModelProperty(value = "老人姓名")
    private String elderName;

    @ApiModelProperty(value = "床位编号")
    private String bedNumber;

    @ApiModelProperty(value = "年龄")
    private Integer age;

    @ApiModelProperty(value = "性别")
    private String sex;

    @ApiModelProperty(value = "护理等级名称")
    private String nursingLevelName;

    @ApiModelProperty(value = "预计服务时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedServerTime;

    @ApiModelProperty(value = "实际服务时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime realServerTime;

    @ApiModelProperty(value = "执行记录")
    private String mark;

    @ApiModelProperty(value = "取消原因")
    private String cancelReason;

    @ApiModelProperty(value = "状态  1待执行 2已执行 3已关闭 ")
    private Integer status;

    @ApiModelProperty(value = "执行图片")
    private String taskImage;

    @ApiModelProperty(value = "执行人")
    private String updater;

    @ApiModelProperty(value = "创建者")
    private String createBy;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新者")
    private String updateBy;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "备注")
    private String remark;
}
