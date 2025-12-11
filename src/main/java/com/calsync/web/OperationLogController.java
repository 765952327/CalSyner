package com.calsync.web;

import com.calsync.domain.OperationLog;
import com.calsync.domain.SyncTask;
import com.calsync.repository.OperationLogRepository;
import com.calsync.repository.SyncTaskRepository;
import com.calsync.service.impl.RadicateClientService;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ops")
public class OperationLogController {
    private final OperationLogRepository repo;
    private final RadicateClientService radicate;
    private final SyncTaskRepository taskRepo;
    
    public OperationLogController(OperationLogRepository repo, RadicateClientService radicate, SyncTaskRepository taskRepo) {
        this.repo = repo;
        this.radicate = radicate;
        this.taskRepo = taskRepo;
    }
    
    @GetMapping("/logs")
    public List<Map<String, Object>> list() {
        Map<Long, String> nameMap = new java.util.HashMap<>();
        for (SyncTask t : taskRepo.findAll()) {
            nameMap.put(t.getId(), t.getTaskName() != null ? t.getTaskName() : ("任务 " + t.getId()));
        }
        return repo.findAll().stream().map(o -> {
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
        }).collect(Collectors.toList());
    }
    
    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam(required = false) String summary,
                                            @RequestParam(required = false) String opType,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) Long from,
                                            @RequestParam(required = false) Long to) {
        Stream<OperationLog> s = repo.findAll().stream();
        if (summary != null && !summary.isEmpty())
            s = s.filter(x -> x.getSummary() != null && x.getSummary().contains(summary));
        if (opType != null && !opType.isEmpty()) s = s.filter(x -> opType.equals(x.getOpType()));
        if (status != null && !status.isEmpty()) s = s.filter(x -> status.equals(x.getStatus()));
        if (from != null) s = s.filter(x -> x.getCreatedAt() != null && x.getCreatedAt().toEpochMilli() >= from);
        if (to != null) s = s.filter(x -> x.getCreatedAt() != null && x.getCreatedAt().toEpochMilli() <= to);
        List<OperationLog> list = s.sorted((a, b) -> {
            long ta = a.getCreatedAt() == null ? 0 : a.getCreatedAt().toEpochMilli();
            long tb = b.getCreatedAt() == null ? 0 : b.getCreatedAt().toEpochMilli();
            return Long.compare(tb, ta);
        }).collect(Collectors.toList());
        Map<Long, String> nameMap = new java.util.HashMap<>();
        for (SyncTask t : taskRepo.findAll()) {
            nameMap.put(t.getId(), t.getTaskName() != null ? t.getTaskName() : ("任务 " + t.getId()));
        }
        return list.stream().map(o -> {
            Map<String, Object> m = new HashMap<>();
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
        }).collect(Collectors.toList());
    }
    
    @PostMapping("/deleteBySummary")
    public ResponseEntity<OperationLog> deleteBySummary(@RequestParam String summary,
                                                        @RequestParam(required = false) Long radicateConfigId) {
//        boolean exists = (radicateConfigId == null)
//                ? radicate.eventSummaryExists(summary)
//                : radicate.eventSummaryExists(summary, radicateConfigId);
//        boolean ok = exists && ((radicateConfigId == null)
//                ? radicate.deleteEventsBySummary(summary)
//                : radicate.deleteEventsBySummary(summary, radicateConfigId));
//        OperationLog log = repo.findAll().stream()
//                .filter(x -> "DELETE_EVENT".equals(x.getOpType()) && summary.equals(x.getSummary()))
//                .reduce((first, second) -> second)
//                .orElseGet(() -> {
//                    OperationLog l = new OperationLog();
//                    l.setOpType("MANUAL_DELETE_EVENT");
//                    l.setSummary(summary);
//                    l.setTargetType("EVENT");
//                    l.setStatus(ok ? "SUCCESS" : (exists ? "FAILED" : "SKIP"));
//                    l.setMessage(ok ? "Deleted" : (exists ? "Delete failed" : "EventMapper not found"));
//                    l.setCreatedAt(Instant.now());
//                    return repo.save(l);
//                });
//        return ResponseEntity.ok(log);
        return ResponseEntity.ok(new OperationLog());
    }
}
