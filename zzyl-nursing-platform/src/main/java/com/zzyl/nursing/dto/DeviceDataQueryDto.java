package com.zzyl.nursing.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 设备数据查询DTO
 */
@Data
@ApiModel("设备数据查询参数")
public class DeviceDataQueryDto {

    @ApiModelProperty(value = "设备id", example = "EUMY0DdzDihH2LNJkdHok1u770", required = true)
    private String iotId;

    @ApiModelProperty(value = "物模型id", example = "HeartRate", required = true)
    private String functionId;

    @ApiModelProperty(value = "查询开始时间毫秒值", example = "1729353600000", required = true, dataType = "Long")
    private Long startTime;

    @ApiModelProperty(value = "查询结束时间毫秒值", example = "1729439999999", required = true, dataType = "Long")
    private Long endTime;
}
