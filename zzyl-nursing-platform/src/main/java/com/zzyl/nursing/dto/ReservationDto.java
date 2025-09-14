package com.zzyl.nursing.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 预约信息DTO对象，用于接收前端请求参数
 */
@Data
@ApiModel("预约信息DTO")
public class ReservationDto {

    // 预约人姓名
    @ApiModelProperty(value = "预约人姓名")
    private String name;

    // 预约人手机号
    @ApiModelProperty(value = "预约人手机号")
    private String mobile;

    // 预约时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "预约时间")
    private LocalDateTime time;

    // 探访人（家人姓名/老人姓名）
    @ApiModelProperty(value = "探访人（家人姓名/老人姓名）")
    private String visitor;

    // 预约类型，0：参观预约，1：探访预约
    @ApiModelProperty(value = "预约类型，0：参观预约，1：探访预约")
    private Integer type;
}
