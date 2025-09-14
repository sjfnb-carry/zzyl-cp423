package com.zzyl.nursing.job;

import com.zzyl.nursing.service.impl.ReservationServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReservationStatusAutoUpdate {
    @Autowired
    private ReservationServiceImpl reservationService;

    public void reservationStatusAutoUpdate() {
        reservationService.updateStatus();
        log.info("预约状态自动更新任务执行完毕");
    }

}
