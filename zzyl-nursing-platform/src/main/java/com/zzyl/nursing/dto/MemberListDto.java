package com.zzyl.nursing.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 老人家属分页查询DTO
 *
 * @author lingma
 * @date 2025-09-21
 */
@Data
@ApiModel("老人家属分页查询DTO")
public class MemberListDto {

    /**
     * 页码
     */
    @ApiModelProperty(value = "页码", example = "1")
    private Integer pageNum;

    /**
     * 页面大小
     */
    @ApiModelProperty(value = "页面大小", example = "10")
    private Integer pageSize;
}
