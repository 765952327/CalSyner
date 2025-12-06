package com.calsync.web;

import com.calsync.domain.SyncTask;
import com.calsync.repository.SyncTaskRepository;
import com.calsync.service.SyncExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/sync/tasks")
public class SyncTaskController {
    private final SyncTaskRepository repo;
    private final SyncExecutionService executor;

    public SyncTaskController(SyncTaskRepository repo, SyncExecutionService executor) {
        this.repo = repo;
        this.executor = executor;
    }

    @GetMapping
    public List<SyncTask> list() { return repo.findByIsDeletedFalse(); }

    @PostMapping
    public SyncTask create(@RequestBody SyncTask task) {
        task.setCreatedAt(Instant.now());
        task.setUpdatedAt(Instant.now());
        task.setSyncStatus("IDLE");
        task.setIsDeleted(Boolean.FALSE);
        return repo.save(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SyncTask> update(@PathVariable Long id, @RequestBody SyncTask body) {
        return repo.findById(id)
                .map(t -> {
                    t.setTaskName(body.getTaskName());
                    t.setDescription(body.getDescription());
                    t.setJiraConfigId(body.getJiraConfigId());
                    t.setRadicateConfigId(body.getRadicateConfigId());
                    t.setJqlExpression(body.getJqlExpression());
                    t.setCronExpression(body.getCronExpression());
                    t.setIsEnabled(body.getIsEnabled());
                    t.setUpdatedAt(Instant.now());
                    return ResponseEntity.ok(repo.save(t));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        java.util.Optional<SyncTask> opt = repo.findById(id);
        if(opt.isPresent()){
            SyncTask t = opt.get();
            t.setIsDeleted(Boolean.TRUE);
            t.setUpdatedAt(Instant.now());
            repo.save(t);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<Void> execute(@PathVariable Long id) {
        java.util.Optional<SyncTask> opt = repo.findById(id).filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()));
        if(opt.isPresent()){
            executor.executeTask(id);
            return ResponseEntity.accepted().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<SyncTask> toggle(@PathVariable Long id) {
        java.util.Optional<SyncTask> opt = repo.findById(id).filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()));
        if(opt.isPresent()){
            SyncTask t = opt.get();
            t.setIsEnabled(Boolean.TRUE.equals(t.getIsEnabled()) ? Boolean.FALSE : Boolean.TRUE);
            t.setUpdatedAt(Instant.now());
            return ResponseEntity.ok(repo.save(t));
        }
        return ResponseEntity.notFound().build();
    }
}
