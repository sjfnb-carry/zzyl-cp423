package com.zzyl.nursing.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 属性列表实体类
 *
 * @author your-name
 * @date 2025-09-21
 */
@Data
@ApiModel(value = "PropertyList", description = "属性列表")
public class PropertyList {

    /**
     * 属性状态信息列表
     */
    @ApiModelProperty(value = "属性状态信息列表")
    private List<PropertyStatusInfo> propertyStatusInfo;
}
