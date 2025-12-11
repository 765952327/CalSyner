package com.calsync.service.impl;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CompletionSyncService {
    

    @Scheduled(fixedDelay = 60000)
    public void sweepCompletedTodos() {

    }
}
