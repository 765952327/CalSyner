package com.calsync.web;

import com.calsync.domain.SyncRecord;
import com.calsync.domain.SyncDetail;
import com.calsync.domain.OperationLog;
import com.calsync.domain.SyncTask;
import com.calsync.repository.SyncRecordRepository;
import com.calsync.repository.SyncDetailRepository;
import com.calsync.repository.OperationLogRepository;
import com.calsync.repository.SyncTaskRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/sync/records")
public class SyncRecordController {
    private final SyncRecordRepository repo;
    private final SyncDetailRepository detailRepo;
    private final OperationLogRepository opRepo;
    private final SyncTaskRepository taskRepo;
    public SyncRecordController(SyncRecordRepository repo, SyncDetailRepository detailRepo, OperationLogRepository opRepo, SyncTaskRepository taskRepo) { this.repo = repo; this.detailRepo = detailRepo; this.opRepo = opRepo; this.taskRepo = taskRepo; }

    @GetMapping
    public java.util.List<java.util.Map<String, Object>> list() {
        java.util.Map<Long, String> nameMap = new java.util.HashMap<>();
        for (SyncTask t : taskRepo.findAll()) {
            nameMap.put(t.getId(), t.getTaskName() != null ? t.getTaskName() : ("任务 " + t.getId()));
        }
        return repo.findAll().stream().map(r -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", r.getId());
            m.put("taskId", r.getTaskId());
            m.put("taskName", nameMap.getOrDefault(r.getTaskId(), null));
            m.put("startTime", r.getStartTime());
            m.put("endTime", r.getEndTime());
            m.put("status", r.getStatus());
            m.put("totalItems", r.getTotalItems());
            m.put("processedItems", r.getProcessedItems());
            m.put("failedItems", r.getFailedItems());
            m.put("errorMessage", r.getErrorMessage());
            m.put("createdAt", r.getCreatedAt());
            return m;
        }).collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/{id}")
    public java.util.Map<String, Object> get(@PathVariable Long id) {
        return repo.findById(id).map(r -> {
            java.util.Map<Long, String> nameMap = new java.util.HashMap<>();
            for (SyncTask t : taskRepo.findAll()) {
                nameMap.put(t.getId(), t.getTaskName() != null ? t.getTaskName() : ("任务 " + t.getId()));
            }
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", r.getId());
            m.put("taskId", r.getTaskId());
            m.put("taskName", nameMap.getOrDefault(r.getTaskId(), null));
            m.put("startTime", r.getStartTime());
            m.put("endTime", r.getEndTime());
            m.put("status", r.getStatus());
            m.put("totalItems", r.getTotalItems());
            m.put("processedItems", r.getProcessedItems());
            m.put("failedItems", r.getFailedItems());
            m.put("errorMessage", r.getErrorMessage());
            m.put("createdAt", r.getCreatedAt());
            return m;
        }).orElse(null);
    }

    @GetMapping("/{id}/details")
    public java.util.List<SyncDetail> details(@PathVariable Long id) {
        return detailRepo.findAll().stream().filter(d -> java.util.Objects.equals(d.getRecordId(), id)).collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/{id}/ops")
    public java.util.List<OperationLog> ops(@PathVariable Long id) {
        return opRepo.findAll().stream().filter(o -> java.util.Objects.equals(o.getRecordId(), id))
                .sorted((a,b) -> {
                    long ta = a.getCreatedAt()==null?0:a.getCreatedAt().toEpochMilli();
                    long tb = b.getCreatedAt()==null?0:b.getCreatedAt().toEpochMilli();
                    return Long.compare(tb, ta);
                }).collect(java.util.stream.Collectors.toList());
    }
}
