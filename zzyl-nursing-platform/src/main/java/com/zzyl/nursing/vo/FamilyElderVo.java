package com.zzyl.nursing.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 老人家属绑定信息VO
 * 
 * @author lingma
 * @date 2025-09-21
 */
@Data
@ApiModel("老人家属绑定信息")
public class FamilyElderVo {

    /**
     * 家属ID
     */
    @ApiModelProperty(value = "家属ID")
    private String mid;

    /**
     * 家属备注/称呼
     */
    @ApiModelProperty(value = "家属备注/称呼")
    private String mremark;

    /**
     * 老人ID
     */
    @ApiModelProperty(value = "老人ID")
    private String elderId;

    /**
     * 老人姓名
     */
    @ApiModelProperty(value = "老人姓名")
    private String name;

    /**
     * 老人图片
     */
    @ApiModelProperty(value = "老人图片")
    private String image;

    /**
     * 床位编号
     */
    @ApiModelProperty(value = "床位编号")
    private String bedNumber;

    /**
     * 房间类型名称
     */
    @ApiModelProperty(value = "房间类型名称")
    private String typeName;

    /**
     * IoT设备ID
     */
    @ApiModelProperty(value = "IoT设备ID")
    private String iotId;

    /**
     * 设备名称
     */
    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    /**
     * 产品Key
     */
    @ApiModelProperty(value = "产品Key")
    private String productKey;
}
