package com.zzyl.nursing.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 家属绑定老人信息VO
 * 
 * @author alexis
 * @date 2025-09-20
 */
@Data
@ApiModel("家属绑定老人信息")
public class BindFamilyMemberVo {

    /**
     * 主键ID
     */
    @ApiModelProperty(value = "主键ID")
    private Long id;

    /**
     * 家属ID
     */
    @ApiModelProperty(value = "家属ID")
    private Long familyMemberId;

    /**
     * 老人ID
     */
    @ApiModelProperty(value = "老人ID")
    private Long elderId;

    /**
     * 老人姓名
     */
    @ApiModelProperty(value = "老人姓名")
    private String elderName;
}
