package com.zzyl.nursing.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 属性状态信息实体类
 *
 * @author your-name
 * @date 2025-09-21
 */
@Data
@ApiModel(value = "PropertyStatusInfo", description = "属性状态信息")
public class PropertyStatusInfo {

    /**
     * 数据类型
     */
    @ApiModelProperty(value = "数据类型", example = "int")
    private String dataType;

    /**
     * 标识符
     */
    @ApiModelProperty(value = "标识符", example = "HeartRate")
    private String identifier;

    /**
     * 名称
     */
    @ApiModelProperty(value = "名称", example = "心率")
    private String name;

    /**
     * 时间戳(毫秒)
     */
    @ApiModelProperty(value = "时间戳(毫秒)", example = "1697096335545")
    private String time;

    /**
     * 单位
     */
    @ApiModelProperty(value = "单位", example = "%")
    private String unit;

    /**
     * 值
     */
    @ApiModelProperty(value = "值", example = "87")
    private String value;
}
