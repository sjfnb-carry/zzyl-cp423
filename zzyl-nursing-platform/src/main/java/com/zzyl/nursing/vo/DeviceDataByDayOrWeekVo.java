package com.zzyl.nursing.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 按天统计设备数据VO
 */
@Data
@ApiModel("按天统计设备数据VO")
public class DeviceDataByDayOrWeekVo {

    @ApiModelProperty(value = "时间点")
    private String dateTime;

    @ApiModelProperty(value = "数据值")
    private BigDecimal dataValue ;
}
