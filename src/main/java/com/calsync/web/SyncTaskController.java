package com.calsync.web;

import com.calsync.domain.SyncTask;
import com.calsync.repository.SyncTaskRepository;
import com.calsync.service.SyncExecutionService;
import org.springframework.http.ResponseEntity;
import com.calsync.repository.ServiceConfigRepository;
import com.calsync.domain.ServiceConfig;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/sync/tasks")
public class SyncTaskController {
    private final SyncTaskRepository repo;
    private final SyncExecutionService executor;
    private final ServiceConfigRepository svcRepo;

    public SyncTaskController(SyncTaskRepository repo, SyncExecutionService executor, ServiceConfigRepository svcRepo) {
        this.repo = repo;
        this.executor = executor;
        this.svcRepo = svcRepo;
    }

    @GetMapping
    public List<SyncTask> list() { return repo.findByIsDeletedFalse(); }

    @GetMapping("/{id}")
    public ResponseEntity<SyncTask> get(@PathVariable Long id) {
        return repo.findById(id)
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

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

    @GetMapping("/fields/source")
    public java.util.List<String> sourceFields(@RequestParam(required=false) Long serviceId){
        return fetchJiraFields(serviceId);
    }

    @GetMapping("/fields/target")
    public java.util.List<String> targetFields(@RequestParam(required=false) Long serviceId){
        return java.util.Arrays.asList("summary","description","location","start","end","url","organizer","externalId","rrule");
    }

    private java.util.List<String> fetchJiraFields(Long serviceId){
        java.util.List<String> fallback = java.util.Arrays.asList("summary","description","duedate","created","updated","priority");
        try{
            ServiceConfig cfg = null;
            if(serviceId!=null){
                cfg = svcRepo.findById(serviceId)
                        .filter(c -> "JIRA".equalsIgnoreCase(c.getServiceType()))
                        .orElse(null);
            }
            if(cfg==null){
                return fallback;
            }
            String baseUrl = cfg.getBaseUrl();
            String email = cfg.getUsername();
            String token = cfg.getApiToken();
            if(baseUrl==null || email==null || token==null) return fallback;
            String url = baseUrl + (baseUrl.endsWith("/")? "" : "/") + "rest/api/3/field";
            OkHttpClient client = new OkHttpClient();
            Request req = new Request.Builder().url(url)
                    .header("Authorization", Credentials.basic(email, token))
                    .header("Accept", "application/json")
                    .get().build();
            try(Response resp = client.newCall(req).execute()){
                if(!resp.isSuccessful()) return fallback;
                String json = resp.body().string();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode arr = mapper.readTree(json);
                java.util.LinkedHashSet<String> out = new java.util.LinkedHashSet<>();
                if(arr.isArray()){
                    for(JsonNode f : arr){
                        String id = f.path("id").asText(null);
                        String name = f.path("name").asText(null);
                        if(id!=null && !id.isEmpty()) out.add(id);
                        else if(name!=null && !name.isEmpty()) out.add(name);
                    }
                }
                if(out.isEmpty()) return fallback;
                // ensure common fields visible first
                java.util.List<String> result = new java.util.ArrayList<>(out);
                result.sort((a,b)->{
                    java.util.List<String> pref = java.util.Arrays.asList("summary","description","duedate","created","updated","priority");
                    int ia = pref.indexOf(a);
                    int ib = pref.indexOf(b);
                    if(ia!=-1 && ib!=-1) return Integer.compare(ia, ib);
                    if(ia!=-1) return -1;
                    if(ib!=-1) return 1;
                    return a.compareToIgnoreCase(b);
                });
                return result;
            }
        }catch(Exception e){
            return fallback;
        }
    }
}
