package com.zzyl.nursing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlertDataDto {
    private Integer pageNum;
    private Integer pageSize;
    private String deviceName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}
