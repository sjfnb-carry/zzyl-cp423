package com.zzyl.nursing.dto;

import lombok.Data;

@Data
public class ElderPageQueryDto {
    private Integer status;
    private String name;
    private String idCardNo;
    private Integer pageNum;
    private Integer pageSize;

}
