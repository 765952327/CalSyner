package com.jirasync.service;

import com.jirasync.domain.OperationLog;
import com.jirasync.repository.OperationLogRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;

@Component
public class CompletionSyncService {
    private final RadicateClientService radicate;
    private final OperationLogRepository logs;

    public CompletionSyncService(RadicateClientService radicate, OperationLogRepository logs) {
        this.radicate = radicate;
        this.logs = logs;
    }

    @Scheduled(fixedDelay = 60000)
    public void sweepCompletedTodos() {
        List<String> completed = radicate.listCompletedTodoSummaries();
        for (String s : completed) {
            if (!radicate.eventSummaryExists(s)) continue;
            radicate.deleteEventsBySummaryForCompletedTodo(s);
        }
    }
}
