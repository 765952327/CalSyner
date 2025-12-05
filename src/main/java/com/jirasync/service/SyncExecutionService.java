package com.jirasync.service;

import com.jirasync.domain.SyncRecord;
import com.jirasync.domain.SyncTask;
import com.jirasync.repository.SyncRecordRepository;
import com.jirasync.repository.SyncTaskRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import com.winstone.sync.EventSpec;

@Service
public class SyncExecutionService {
    private final SyncTaskRepository taskRepo;
    private final SyncRecordRepository recordRepo;
    private final com.jirasync.repository.SyncDetailRepository syncDetailRepo;
    private final JiraClientService jira;
    private final RadicateClientService radicate;

    public SyncExecutionService(SyncTaskRepository taskRepo, SyncRecordRepository recordRepo, com.jirasync.repository.SyncDetailRepository syncDetailRepo, JiraClientService jira, RadicateClientService radicate) {
        this.taskRepo = taskRepo;
        this.recordRepo = recordRepo;
        this.syncDetailRepo = syncDetailRepo;
        this.jira = jira;
        this.radicate = radicate;
    }

    @Transactional
    public void executeTask(Long taskId) {
        SyncTask task = taskRepo.findById(taskId).orElse(null);
        if (task == null) return;
        SyncRecord rec = new SyncRecord();
        rec.setTaskId(task.getId());
        rec.setStartTime(Instant.now());
        rec.setStatus("RUNNING");
        rec.setCreatedAt(Instant.now());
        recordRepo.save(rec);
        try {
            List<EventSpec> specs = jira.fetchByJql(task.getJiraConfigId(), task.getJqlExpression());
            if (specs == null) specs = new ArrayList<>();
            List<RadicateSyncResult> results = radicate.upsertAndCollect(specs, rec.getId(), task.getId(), task.getRadicateConfigId());
            int ok = 0, fail = 0;
            for (RadicateSyncResult r : results) {
                com.jirasync.domain.SyncDetail d = new com.jirasync.domain.SyncDetail();
                d.setRecordId(rec.getId());
                d.setItemId(r.getSummary());
                d.setItemSummary(r.getSummary());
                d.setAction("UPSERT_" + r.getTargetType());
                d.setStatus(r.getCode() >= 200 && r.getCode() < 300 ? "SUCCESS" : "FAILED");
                d.setTargetType(r.getTargetType());
                d.setRadicateUid(r.getUid());
                d.setPayload(r.getPayload());
                d.setCreatedAt(Instant.now());
                if ("SUCCESS".equals(d.getStatus())) ok++; else fail++;
                syncDetailRepo.save(d);
            }
            rec.setTotalItems(results.size());
            rec.setProcessedItems(ok);
            rec.setFailedItems(fail);
            rec.setStatus(fail == 0 ? "SUCCESS" : (ok > 0 ? "PARTIAL" : "FAILED"));
        } catch (Exception e) {
            rec.setStatus("FAILED");
            rec.setErrorMessage(e.getMessage());
        } finally {
            rec.setEndTime(Instant.now());
            recordRepo.save(rec);
            task.setLastSyncTime(Instant.now());
            task.setSyncStatus(rec.getStatus());
            taskRepo.save(task);
        }
    }
}
