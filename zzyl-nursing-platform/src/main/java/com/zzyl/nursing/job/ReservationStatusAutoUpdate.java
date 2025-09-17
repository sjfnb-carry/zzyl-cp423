package com.zzyl.nursing.job;

import com.zzyl.nursing.service.impl.ReservationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReservationStatusAutoUpdate {
    @Autowired
    private ReservationServiceImpl reservationService;

    public void reservationStatusAutoUpdate() {
        reservationService.updateStatus();
    }
}
