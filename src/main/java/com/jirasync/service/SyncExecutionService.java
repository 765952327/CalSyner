package com.jirasync.service;

import com.jirasync.domain.SyncRecord;
import com.jirasync.domain.SyncTask;
import com.jirasync.repository.SyncRecordRepository;
import com.jirasync.repository.SyncTaskRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import com.winstone.sync.EventSpec;
import com.jirasync.web.dto.FieldMappingDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jirasync.domain.ServiceConfig;
import com.jirasync.repository.ServiceConfigRepository;
import com.jirasync.service.datasource.DataSourceAdapter;
import com.jirasync.service.datasource.JiraDataSourceAdapter;
import com.jirasync.service.datasource.CustomScriptDataSourceAdapter;

@Service
public class SyncExecutionService {
    private final SyncTaskRepository taskRepo;
    private final SyncRecordRepository recordRepo;
    private final com.jirasync.repository.SyncDetailRepository syncDetailRepo;
    private final JiraClientService jira;
    private final RadicateClientService radicate;
    private final ServiceConfigRepository serviceConfigs;

    public SyncExecutionService(SyncTaskRepository taskRepo, SyncRecordRepository recordRepo, com.jirasync.repository.SyncDetailRepository syncDetailRepo, JiraClientService jira, RadicateClientService radicate, ServiceConfigRepository serviceConfigs) {
        this.taskRepo = taskRepo;
        this.recordRepo = recordRepo;
        this.syncDetailRepo = syncDetailRepo;
        this.jira = jira;
        this.radicate = radicate;
        this.serviceConfigs = serviceConfigs;
    }

    @Transactional
    public void executeTask(Long taskId) {
        SyncTask task = taskRepo.findById(taskId).orElse(null);
        if (task == null) return;
        SyncRecord rec = new SyncRecord();
        rec.setTaskId(task.getId());
        rec.setStartTime(Instant.now());
        rec.setStatus("RUNNING");
        rec.setCreatedAt(Instant.now());
        recordRepo.save(rec);
        try {
            List<FieldMappingDTO> mappings = parseMappings(task.getDescription());
            ServiceConfig srcCfg = serviceConfigs.findById(task.getJiraConfigId()).orElse(null);
            DataSourceAdapter adapter = pickAdapter(srcCfg);
            List<EventSpec> specs = adapter.fetch(srcCfg, task, mappings);
            if (specs == null) specs = new ArrayList<>();
            List<RadicateSyncResult> results = radicate.upsertAndCollect(specs, rec.getId(), task.getId(), task.getRadicateConfigId());
            int ok = 0, fail = 0;
            for (RadicateSyncResult r : results) {
                com.jirasync.domain.SyncDetail d = new com.jirasync.domain.SyncDetail();
                d.setRecordId(rec.getId());
                d.setItemId(r.getSummary());
                d.setItemSummary(r.getSummary());
                d.setAction("UPSERT_" + r.getTargetType());
                d.setStatus(r.getCode() >= 200 && r.getCode() < 300 ? "SUCCESS" : "FAILED");
                d.setTargetType(r.getTargetType());
                d.setRadicateUid(r.getUid());
                d.setPayload(r.getPayload());
                d.setCreatedAt(Instant.now());
                if ("SUCCESS".equals(d.getStatus())) ok++; else fail++;
                syncDetailRepo.save(d);
            }
            rec.setTotalItems(results.size());
            rec.setProcessedItems(ok);
            rec.setFailedItems(fail);
            rec.setStatus(fail == 0 ? "SUCCESS" : (ok > 0 ? "PARTIAL" : "FAILED"));
        } catch (Exception e) {
            rec.setStatus("FAILED");
            rec.setErrorMessage(e.getMessage());
        } finally {
            rec.setEndTime(Instant.now());
            recordRepo.save(rec);
            task.setLastSyncTime(Instant.now());
            task.setSyncStatus(rec.getStatus());
            taskRepo.save(task);
        }
    }

    private List<FieldMappingDTO> parseMappings(String json) {
        try {
            if (json == null || json.trim().isEmpty()) return java.util.Collections.emptyList();
            ObjectMapper mapper = new ObjectMapper();
            java.util.List<FieldMappingDTO> arr = new java.util.ArrayList<>();
            FieldMappingDTO[] a = mapper.readValue(json, FieldMappingDTO[].class);
            if (a != null) java.util.Collections.addAll(arr, a);
            return arr;
        } catch (Exception ignored) {}
        return java.util.Collections.emptyList();
    }

    private DataSourceAdapter pickAdapter(ServiceConfig cfg) {
        String t = cfg != null ? cfg.getServiceType() : "JIRA";
        if ("CUSTOM".equalsIgnoreCase(t)) return new CustomScriptDataSourceAdapter();
        return new JiraDataSourceAdapter();
    }
}
