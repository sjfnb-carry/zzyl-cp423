
package com.zzyl.nursing.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 老人信息VO对象
 * 
 * @author lingma
 * @date 2025-09-21
 */
@Data
@ApiModel("老人信息视图对象")
public class ElderInfoVo {
    
    /**
     * 家属ID
     */
    @ApiModelProperty("家属ID")
    private Long mid;
    
    /**
     * 家属备注
     */
    @ApiModelProperty("家属备注")
    private String mremark;
    
    /**
     * 老人ID
     */
    @ApiModelProperty("老人ID")
    private Long elderId;
    
    /**
     * 老人姓名
     */
    @ApiModelProperty("老人姓名")
    private String name;
    
    /**
     * 老人图片
     */
    @ApiModelProperty("老人图片")
    private String image;
    
    /**
     * 床位编号
     */
    @ApiModelProperty("床位编号")
    private String bedNumber;
    
    /**
     * 设备ID
     */
    @ApiModelProperty("设备ID")
    private String iotId;
    
    /**
     * 设备名称
     */
    @ApiModelProperty("设备名称")
    private String deviceName;
    
    /**
     * 产品Key
     */
    @ApiModelProperty("产品Key")
    private String productKey;
    
    /**
     * 房间类型名称
     */
    @ApiModelProperty("房间类型名称")
    private String typeName;
}