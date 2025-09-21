package com.zzyl.nursing.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.zzyl.common.annotation.Excel;
import com.zzyl.common.core.domain.BaseEntity;

/**
 * 老人-家属关联中间对象 family_member_elder
 * 
 * @author alexis
 * @date 2025-09-20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("老人-家属关联中间实体")
@TableName("family_member_elder")
public class FamilyMemberElder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    // 主键ID
    @ApiModelProperty("主键ID")
    private Long id;

    // 老人ID
    @Excel(name = "老人ID")
    @ApiModelProperty("老人ID")
    private Long elderId;

    // 家属ID
    @Excel(name = "家属ID")
    @ApiModelProperty("家属ID")
    private Long familyMemberId;

    /**
     * 称呼/备注
     */
    @ApiModelProperty("备注  对老人的称呼")
    private String remark;
}
