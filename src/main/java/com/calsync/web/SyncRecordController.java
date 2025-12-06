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
    public java.util.List<java.util.Map<String, Object>> ops(@PathVariable Long id,
                                                             @RequestParam(required=false) String summary,
                                                             @RequestParam(required=false) String opType,
                                                             @RequestParam(required=false) String status) {
        java.util.Map<Long, String> nameMap = new java.util.HashMap<>();
        for (SyncTask t : taskRepo.findAll()) {
            nameMap.put(t.getId(), t.getTaskName() != null ? t.getTaskName() : ("任务 " + t.getId()));
        }
        java.util.stream.Stream<OperationLog> s = opRepo.findAll().stream().filter(o -> java.util.Objects.equals(o.getRecordId(), id));
        if (summary != null && !summary.isEmpty()) s = s.filter(x -> x.getSummary()!=null && x.getSummary().contains(summary));
        if (opType != null && !opType.isEmpty()) s = s.filter(x -> opType.equals(x.getOpType()));
        if (status != null && !status.isEmpty()) s = s.filter(x -> status.equals(x.getStatus()));
        java.util.List<OperationLog> list = s
                .sorted((a,b) -> {
                    long ta = a.getCreatedAt()==null?0:a.getCreatedAt().toEpochMilli();
                    long tb = b.getCreatedAt()==null?0:b.getCreatedAt().toEpochMilli();
                    return Long.compare(tb, ta);
                }).collect(java.util.stream.Collectors.toList());
        return list.stream().map(o -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", o.getId());
            m.put("opType", o.getOpType());
            m.put("summary", o.getSummary());
            m.put("targetType", o.getTargetType());
            m.put("status", o.getStatus());
            m.put("message", o.getMessage());
            m.put("recordId", o.getRecordId());
            m.put("taskId", o.getTaskId());
            m.put("taskName", nameMap.getOrDefault(o.getTaskId(), null));
            m.put("createdAt", o.getCreatedAt());
            return m;
        }).collect(java.util.stream.Collectors.toList());
    }
}
