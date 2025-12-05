package com.jirasync.web;

import com.jirasync.domain.ServiceConfig;
import com.jirasync.repository.ServiceConfigRepository;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/config/services")
public class ServiceConfigController {
    private final ServiceConfigRepository repo;
    public ServiceConfigController(ServiceConfigRepository repo) { this.repo = repo; }

    @GetMapping
    public List<ServiceConfig> list() { return repo.findAll(); }

    @PostMapping
    public ServiceConfig create(@RequestBody ServiceConfig cfg) {
        cfg.setCreatedAt(Instant.now());
        cfg.setUpdatedAt(Instant.now());
        cfg.setConnectionStatus("UNKNOWN");
        return repo.save(cfg);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceConfig> update(@PathVariable Long id, @RequestBody ServiceConfig body) {
        return repo.findById(id)
                .map(cfg -> {
                    cfg.setServiceType(body.getServiceType());
                    cfg.setServiceName(body.getServiceName());
                    cfg.setBaseUrl(body.getBaseUrl());
                    cfg.setUsername(body.getUsername());
                    cfg.setPassword(body.getPassword());
                    cfg.setApiToken(body.getApiToken());
                    cfg.setUpdatedAt(Instant.now());
                    return ResponseEntity.ok(repo.save(cfg));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<ServiceConfig> test(@PathVariable Long id) {
        return repo.findById(id).map(cfg -> {
            boolean ok = false;
            try {
                OkHttpClient client = new OkHttpClient();
                Request req = new Request.Builder().url(cfg.getBaseUrl()).get().build();
                try (Response resp = client.newCall(req).execute()) {
                    ok = resp.isSuccessful();
                }
            } catch (Exception ignored) {}
            cfg.setConnectionStatus(ok ? "OK" : "FAILED");
            cfg.setLastTestTime(Instant.now());
            cfg.setUpdatedAt(Instant.now());
            return ResponseEntity.ok(repo.save(cfg));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
