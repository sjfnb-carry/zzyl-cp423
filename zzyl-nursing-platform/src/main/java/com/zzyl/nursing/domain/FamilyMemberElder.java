package com.zzyl.nursing.domain;

import com.zzyl.common.annotation.Excel;
import com.zzyl.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 家庭成员与老人关联对象 family_member_elder
 *
 * @author alexis
 * @date 2025-09-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("家庭成员与老人关联实体")
public class FamilyMemberElder extends BaseEntity {
    private static final long serialVersionUID = 1L;

    // 主键id
    @ApiModelProperty("主键id")
    private Long id;

    // 老人id
    @Excel(name = "老人id")
    @ApiModelProperty("老人id")
    private Long elderId;

    // 家属id
    @Excel(name = "家属id")
    @ApiModelProperty("家属id")
    private Long familyMemberId;

}
