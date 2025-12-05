package com.jirasync.web;

import com.jirasync.domain.OperationLog;
import com.jirasync.repository.OperationLogRepository;
import com.jirasync.service.RadicateClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/ops")
public class OperationLogController {
    private final OperationLogRepository repo;
    private final RadicateClientService radicate;

    public OperationLogController(OperationLogRepository repo, RadicateClientService radicate) {
        this.repo = repo;
        this.radicate = radicate;
    }

    @GetMapping("/logs")
    public List<OperationLog> list() { return repo.findAll(); }

    @GetMapping("/search")
    public List<OperationLog> search(@RequestParam(required=false) String summary,
                                     @RequestParam(required=false) String opType,
                                     @RequestParam(required=false) String status,
                                     @RequestParam(required=false) Long from,
                                     @RequestParam(required=false) Long to) {
        java.util.stream.Stream<OperationLog> s = repo.findAll().stream();
        if (summary != null && !summary.isEmpty()) s = s.filter(x -> x.getSummary()!=null && x.getSummary().contains(summary));
        if (opType != null && !opType.isEmpty()) s = s.filter(x -> opType.equals(x.getOpType()));
        if (status != null && !status.isEmpty()) s = s.filter(x -> status.equals(x.getStatus()));
        if (from != null) s = s.filter(x -> x.getCreatedAt()!=null && x.getCreatedAt().toEpochMilli() >= from);
        if (to != null) s = s.filter(x -> x.getCreatedAt()!=null && x.getCreatedAt().toEpochMilli() <= to);
        return s.sorted((a,b) -> {
            long ta = a.getCreatedAt()==null?0:a.getCreatedAt().toEpochMilli();
            long tb = b.getCreatedAt()==null?0:b.getCreatedAt().toEpochMilli();
            return Long.compare(tb, ta);
        }).collect(java.util.stream.Collectors.toList());
    }

    @PostMapping("/deleteBySummary")
    public ResponseEntity<OperationLog> deleteBySummary(@RequestParam String summary) {
        boolean exists = radicate.eventSummaryExists(summary);
        boolean ok = exists && radicate.deleteEventsBySummary(summary);
        OperationLog log = repo.findAll().stream()
                .filter(x -> "DELETE_EVENT".equals(x.getOpType()) && summary.equals(x.getSummary()))
                .reduce((first, second) -> second)
                .orElseGet(() -> {
                    OperationLog l = new OperationLog();
                    l.setOpType("MANUAL_DELETE_EVENT");
                    l.setSummary(summary);
                    l.setTargetType("EVENT");
                    l.setStatus(ok ? "SUCCESS" : (exists ? "FAILED" : "SKIP"));
                    l.setMessage(ok ? "Deleted" : (exists ? "Delete failed" : "Event not found"));
                    l.setCreatedAt(Instant.now());
                    return repo.save(l);
                });
        return ResponseEntity.ok(log);
    }
}
