package com.jirasync.config;

import com.jirasync.domain.SyncTask;
import com.jirasync.repository.SyncTaskRepository;
import com.jirasync.service.SyncExecutionService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;

@EnableScheduling
@Component
public class SchedulerConfig {
    private final SyncTaskRepository taskRepo;
    private final SyncExecutionService executor;

    public SchedulerConfig(SyncTaskRepository taskRepo, SyncExecutionService executor) {
        this.taskRepo = taskRepo;
        this.executor = executor;
    }

    @Scheduled(fixedDelay = 60000)
    public void scanAndRun() {
        List<SyncTask> tasks = taskRepo.findByIsEnabledTrue();
        Instant now = Instant.now();
        for (SyncTask t : tasks) {
            if (t.getNextSyncTime() == null || !t.getNextSyncTime().isAfter(now)) {
                executor.executeTask(t.getId());
            }
        }
    }
}
