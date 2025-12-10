package com.calsync.service;

import com.calsync.repository.OperationLogRepository;
import com.calsync.repository.SyncTaskRepository;
import com.calsync.domain.SyncTask;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CompletionSyncService {
    private final RadicateClientService radicate;
    private final OperationLogRepository logs;
    private final SyncTaskRepository tasks;

    public CompletionSyncService(RadicateClientService radicate, OperationLogRepository logs, SyncTaskRepository tasks) {
        this.radicate = radicate;
        this.logs = logs;
        this.tasks = tasks;
    }

    @Scheduled(fixedDelay = 60000)
    public void sweepCompletedTodos() {
//        List<SyncTask> enabled = tasks.findByIsEnabledTrue();
//        if (enabled == null || enabled.isEmpty()) return;
//        for (SyncTask t : enabled) {
//            Long rid = t.getRadicaleConfigId();
//            List<String> completed = (rid == null)
//                    ? radicate.listCompletedTodoSummaries()
//                    : radicate.listCompletedTodoSummaries(rid);
//            for (String s : completed) {
//                boolean exists = (rid == null)
//                        ? radicate.eventSummaryExists(s)
//                        : radicate.eventSummaryExists(s, rid);
//                if (!exists) continue;
//                if (rid == null) radicate.deleteEventsBySummaryForCompletedTodo(s);
//                else radicate.deleteEventsBySummaryForCompletedTodo(s, rid);
//            }
//        }
    }
}
