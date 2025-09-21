package com.zzyl.nursing.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 设备属性状态数据实体类
 *
 * @author your-name
 * @date 2025-09-21
 */
@Data
@ApiModel(value = "DevicePropertyStatusData", description = "设备属性状态数据")
public class DevicePropertyStatusData {

    /**
     * 属性列表信息
     */
    @ApiModelProperty(value = "属性列表信息")
    private PropertyList list;
}
