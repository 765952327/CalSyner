package com.jirasync.web;

import com.jirasync.domain.SyncTask;
import com.jirasync.repository.SyncTaskRepository;
import com.jirasync.service.SyncExecutionService;
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
    public List<SyncTask> list() { return repo.findAll(); }

    @PostMapping
    public SyncTask create(@RequestBody SyncTask task) {
        task.setCreatedAt(Instant.now());
        task.setUpdatedAt(Instant.now());
        task.setSyncStatus("IDLE");
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
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<Void> execute(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        executor.executeTask(id);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<SyncTask> toggle(@PathVariable Long id) {
        return repo.findById(id)
                .map(t -> {
                    t.setIsEnabled(Boolean.TRUE.equals(t.getIsEnabled()) ? Boolean.FALSE : Boolean.TRUE);
                    t.setUpdatedAt(Instant.now());
                    return ResponseEntity.ok(repo.save(t));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
