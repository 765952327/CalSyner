package com.calsync.web;

import com.calsync.domain.ServiceConfig;
import com.calsync.repository.ServiceConfigRepository;
import com.calsync.service.datasource.CustomScriptDataSourceAdapter;
import com.calsync.sync.Event;
import com.calsync.web.dto.FieldMappingDTO;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.calsync.sync.radicale.RadicaleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config/services")
public class ServiceConfigController {
    @Autowired
    private ServiceConfigRepository repo;
    
    @GetMapping
    public List<ServiceConfig> list() {
        return repo.findAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ServiceConfig> get(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
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
            if (cfg.getServiceType() == com.calsync.domain.ServiceType.RADICALE) {
                RadicaleClient client = new RadicaleClient(cfg.getBaseUrl(), cfg.getUsername(), cfg.getPassword());
                ok = client.ping();
            } else {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request req = new Request.Builder().url(cfg.getBaseUrl()).get().build();
                    try (Response resp = client.newCall(req).execute()) {
                        ok = resp.isSuccessful();
                    }
                } catch (Exception ignored) {
                }
            }
            cfg.setConnectionStatus(ok ? "OK" : "FAILED");
            cfg.setLastTestTime(Instant.now());
            cfg.setUpdatedAt(Instant.now());
            return ResponseEntity.ok(repo.save(cfg));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{id}/script")
    public ResponseEntity<ServiceConfig> uploadScript(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String code = body != null ? body.get("code") : null;
        return repo.findById(id)
                .map(cfg -> {
                    cfg.setApiToken(code);
                    cfg.setUpdatedAt(Instant.now());
                    return ResponseEntity.ok(repo.save(cfg));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{id}/script/test")
    public ResponseEntity<Map<String, Object>> testScript(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        return repo.findById(id).map(cfg -> {
            String code = body != null ? (String) body.get("code") : null;
            if (code != null && !code.trim().isEmpty()) {
                cfg.setApiToken(code);
                cfg.setUpdatedAt(Instant.now());
                repo.save(cfg);
            }
            CustomScriptDataSourceAdapter adapter = new CustomScriptDataSourceAdapter();
            com.calsync.domain.SyncTask fake = new com.calsync.domain.SyncTask();
            fake.setTaskName("ScriptTest");
            fake.setJqlExpression("project = TEST");
            java.util.List<FieldMappingDTO> mappings = java.util.Collections.emptyList();
            java.util.List<Event> specs = adapter.fetch(cfg, fake, mappings);
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("count", specs.size());
            result.put("sample", specs.isEmpty() ? null : specs.get(0).getSummary());
            return ResponseEntity.ok(result);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
