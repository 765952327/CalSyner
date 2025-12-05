package com.calsync.web;

import com.calsync.domain.SyncRecord;
import com.calsync.domain.SyncDetail;
import com.calsync.domain.OperationLog;
import com.calsync.repository.SyncRecordRepository;
import com.calsync.repository.SyncDetailRepository;
import com.calsync.repository.OperationLogRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sync/records")
public class SyncRecordController {
    private final SyncRecordRepository repo;
    private final SyncDetailRepository detailRepo;
    private final OperationLogRepository opRepo;
    public SyncRecordController(SyncRecordRepository repo, SyncDetailRepository detailRepo, OperationLogRepository opRepo) { this.repo = repo; this.detailRepo = detailRepo; this.opRepo = opRepo; }

    @GetMapping
    public List<SyncRecord> list() { return repo.findAll(); }

    @GetMapping("/{id}")
    public SyncRecord get(@PathVariable Long id) { return repo.findById(id).orElse(null); }

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
