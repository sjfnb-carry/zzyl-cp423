package com.zzyl.nursing.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "小程序绑定老人")
@Data
public class BindFamilyMemberRequestDto {

    /**
     * 老人身份证号
     */
    @ApiModelProperty(value = "老人身份证号")
    private String idCard;

    /**
     * 老人姓名
     */
    @ApiModelProperty(value = "老人姓名")
    private String name;

    /**
     * 称呼/备注
     */
    private String remark;
}