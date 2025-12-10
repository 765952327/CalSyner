package com.calsync.service;

import com.calsync.domain.OperationLog;
import com.calsync.domain.ServiceConfig;
import com.calsync.domain.ServiceType;
import com.calsync.repository.OperationLogRepository;
import com.calsync.repository.ServiceConfigRepository;
import com.calsync.sync.Event;
import com.calsync.sync.EventPublisher;
import com.calsync.sync.caldav.BiweeklyFormatter;
import com.calsync.sync.caldav.CalDavConfig;
import com.calsync.sync.radicale.RadicaleClient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;

@Service
public class RadicateClientService implements EventPublisher {
    private final BiweeklyFormatter formatter = new BiweeklyFormatter();
    private final OperationLogRepository logs;
    private final ServiceConfigRepository serviceConfigs;
    
    public void upsertEvents(List<Event> specs) {
        RadicaleClient client = new RadicaleClient(CalDavConfig.RADICALE_URL, CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD);
        Set<String> desired = new HashSet<>();
        for (Event s : specs) desired.add(s.getSummary());
        List<String> existing = client.listSummaries();
        for (String ex : existing) {
            if (!desired.contains(ex)) client.deleteBySummary(ex);
        }
        for (Event spec : specs) {
            String uid = uidFromSummary(spec.getSummary());
            String ics = formatter.format(spec, uid);
            client.replaceBySummary(ics, uid, spec.getSummary());
            String todoIcs = formatter.formatTodo(spec, uid + "-todo");
            client.replaceTodoBySummary(todoIcs, uid + "-todo", spec.getSummary());
        }
    }
    
    @Override
    public void upsert(List<Event> specs) {
        upsertEvents(specs);
    }
    
    public RadicateClientService(OperationLogRepository logs, ServiceConfigRepository serviceConfigs) {
        this.logs = logs;
        this.serviceConfigs = serviceConfigs;
    }
    
    private ServiceConfig getConfig(Long id, ServiceType expectType) {
        if (id == null) return null;
        return serviceConfigs.findById(id).filter(c -> expectType == c.getServiceType()).orElse(null);
    }
    
    public List<RadicateSyncResult> upsertAndCollect(List<Event> specs, Long recordId, Long taskId) {
        List<RadicateSyncResult> out = new ArrayList<>();
        Set<String> desired = new HashSet<>();
        for (Event s : specs) desired.add(s.getSummary());
        RadicaleClient client = new RadicaleClient(CalDavConfig.RADICALE_URL, CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD);
        List<String> existing = client.listSummaries();
        for (String ex : existing) {
            if (!desired.contains(ex)) client.deleteBySummary(ex);
        }
        for (Event spec : specs) {
            String uid = uidFromSummary(spec.getSummary());
            String ics = formatter.format(spec, uid);
            String prevEventIcs = client.getEventIcsBySummary(spec.getSummary());
            boolean existedEvent = prevEventIcs != null;
            boolean changedEvent = !existedEvent || !client.normalize(prevEventIcs).equals(client.normalize(ics));
            if (changedEvent) {
                int code = client.replaceBySummary(ics, uid, spec.getSummary());
                RadicateSyncResult ev = new RadicateSyncResult();
                ev.setSummary(spec.getSummary());
                ev.setUid(uid);
                ev.setCode(code);
                ev.setPayload(ics);
                ev.setTargetType("EVENT");
                out.add(ev);
                
                OperationLog logEv = new OperationLog();
                logEv.setOpType(existedEvent ? "UPDATE_EVENT" : "CREATE_EVENT");
                logEv.setSummary(spec.getSummary());
                logEv.setTargetType("EVENT");
                logEv.setRadicateUid(uid);
                logEv.setStatus(code >= 200 && code < 300 ? "SUCCESS" : "FAILED");
                logEv.setMessage("code=" + code);
                logEv.setCreatedAt(java.time.Instant.now());
                logEv.setRecordId(recordId);
                logEv.setTaskId(taskId);
                logs.save(logEv);
            }
            
            String todoUid = uid + "-todo";
            String todoIcs = formatter.formatTodo(spec, todoUid);
            String prevTodoIcs = client.getTodoIcsBySummary(spec.getSummary());
            boolean existedTodo = prevTodoIcs != null;
            boolean changedTodo = !existedTodo || !client.normalize(prevTodoIcs).equals(client.normalize(todoIcs));
            if (changedTodo) {
                int todoCode = client.replaceTodoBySummary(todoIcs, todoUid, spec.getSummary());
                RadicateSyncResult td = new RadicateSyncResult();
                td.setSummary(spec.getSummary());
                td.setUid(todoUid);
                td.setCode(todoCode);
                td.setPayload(todoIcs);
                td.setTargetType("TODO");
                out.add(td);
                
                OperationLog logTd = new OperationLog();
                logTd.setOpType(existedTodo ? "UPDATE_TODO" : "CREATE_TODO");
                logTd.setSummary(spec.getSummary());
                logTd.setTargetType("TODO");
                logTd.setRadicateUid(todoUid);
                logTd.setStatus(todoCode >= 200 && todoCode < 300 ? "SUCCESS" : "FAILED");
                logTd.setMessage("code=" + todoCode);
                logTd.setCreatedAt(java.time.Instant.now());
                logTd.setRecordId(recordId);
                logTd.setTaskId(taskId);
                logs.save(logTd);
            }
        }
        return out;
    }
    
    public List<RadicateSyncResult> upsertAndCollect(List<Event> specs, Long recordId, Long taskId, Long radicateConfigId) {
        ServiceConfig cfg = getConfig(radicateConfigId, ServiceType.RADICALE);
        String base = cfg != null ? cfg.getBaseUrl() : CalDavConfig.RADICALE_URL;
        String user = cfg != null ? cfg.getUsername() : CalDavConfig.RADICALE_USERNAME;
        String pass = cfg != null ? cfg.getPassword() : CalDavConfig.RADICALE_PASSWORD;
        List<RadicateSyncResult> out = new ArrayList<>();
        Set<String> desired = new HashSet<>();
        for (Event s : specs) desired.add(s.getSummary());
        RadicaleClient client = new RadicaleClient(base, user, pass);
        List<String> existing = client.listSummaries();
        for (String ex : existing) {
            if (!desired.contains(ex)) client.deleteBySummary(ex);
        }
        for (Event spec : specs) {
            String uid = uidFromSummary(spec.getSummary());
            String ics = formatter.format(spec, uid);
            String prevEventIcs = client.getEventIcsBySummary(spec.getSummary());
            boolean existedEvent = prevEventIcs != null;
            boolean changedEvent = !existedEvent || !client.normalize(prevEventIcs).equals(client.normalize(ics));
            if (changedEvent) {
                int code = client.replaceBySummary(ics, uid, spec.getSummary());
                RadicateSyncResult ev = new RadicateSyncResult();
                ev.setSummary(spec.getSummary());
                ev.setUid(uid);
                ev.setCode(code);
                ev.setPayload(ics);
                ev.setTargetType("EVENT");
                out.add(ev);
                
                OperationLog logEv = new OperationLog();
                logEv.setOpType(existedEvent ? "UPDATE_EVENT" : "CREATE_EVENT");
                logEv.setSummary(spec.getSummary());
                logEv.setTargetType("EVENT");
                logEv.setRadicateUid(uid);
                logEv.setStatus(code >= 200 && code < 300 ? "SUCCESS" : "FAILED");
                logEv.setMessage("code=" + code);
                logEv.setCreatedAt(java.time.Instant.now());
                logEv.setRecordId(recordId);
                logEv.setTaskId(taskId);
                logs.save(logEv);
            }
            
            String todoUid = uid + "-todo";
            String todoIcs = formatter.formatTodo(spec, todoUid);
            String prevTodoIcs = client.getTodoIcsBySummary(spec.getSummary());
            boolean existedTodo = prevTodoIcs != null;
            boolean changedTodo = !existedTodo || !client.normalize(prevTodoIcs).equals(client.normalize(todoIcs));
            if (changedTodo) {
                int todoCode = client.replaceTodoBySummary(todoIcs, todoUid, spec.getSummary());
                RadicateSyncResult td = new RadicateSyncResult();
                td.setSummary(spec.getSummary());
                td.setUid(todoUid);
                td.setCode(todoCode);
                td.setPayload(todoIcs);
                td.setTargetType("TODO");
                out.add(td);
                
                OperationLog logTd = new OperationLog();
                logTd.setOpType(existedTodo ? "UPDATE_TODO" : "CREATE_TODO");
                logTd.setSummary(spec.getSummary());
                logTd.setTargetType("TODO");
                logTd.setRadicateUid(todoUid);
                logTd.setStatus(todoCode >= 200 && todoCode < 300 ? "SUCCESS" : "FAILED");
                logTd.setMessage("code=" + todoCode);
                logTd.setCreatedAt(java.time.Instant.now());
                logTd.setRecordId(recordId);
                logTd.setTaskId(taskId);
                logs.save(logTd);
            }
        }
        return out;
    }
    
    
    public List<String> listCompletedTodoSummaries() {
        RadicaleClient client = new RadicaleClient(CalDavConfig.RADICALE_URL, CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD);
        return client.listCompletedTodoSummaries();
    }
    
    public List<String> listCompletedTodoSummaries(Long radicateConfigId) {
        ServiceConfig cfg = getConfig(radicateConfigId, ServiceType.RADICALE);
        String base = cfg != null ? cfg.getBaseUrl() : CalDavConfig.RADICALE_URL;
        String user = cfg != null ? cfg.getUsername() : CalDavConfig.RADICALE_USERNAME;
        String pass = cfg != null ? cfg.getPassword() : CalDavConfig.RADICALE_PASSWORD;
        RadicaleClient client = new RadicaleClient(base, user, pass);
        return client.listCompletedTodoSummaries();
    }
    
    public boolean deleteEventsBySummary(String summary) {
        RadicaleClient client = new RadicaleClient(CalDavConfig.RADICALE_URL, CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD);
        int code = client.deleteEventsBySummary(summary);
        OperationLog log = new OperationLog();
        log.setOpType("DELETE_EVENT");
        log.setSummary(summary);
        log.setTargetType("EVENT");
        log.setStatus(code >= 200 && code < 300 ? "SUCCESS" : "FAILED");
        log.setMessage("code=" + code);
        log.setCreatedAt(java.time.Instant.now());
        logs.save(log);
        return code >= 200 && code < 300;
    }
    
    public boolean deleteEventsBySummary(String summary, Long radicateConfigId) {
        ServiceConfig cfg = getConfig(radicateConfigId, ServiceType.RADICALE);
        String base = cfg != null ? cfg.getBaseUrl() : CalDavConfig.RADICALE_URL;
        String user = cfg != null ? cfg.getUsername() : CalDavConfig.RADICALE_USERNAME;
        String pass = cfg != null ? cfg.getPassword() : CalDavConfig.RADICALE_PASSWORD;
        RadicaleClient client = new RadicaleClient(base, user, pass);
        int code = client.deleteEventsBySummary(summary);
        OperationLog log = new OperationLog();
        log.setOpType("DELETE_EVENT");
        log.setSummary(summary);
        log.setTargetType("EVENT");
        log.setStatus(code >= 200 && code < 300 ? "SUCCESS" : "FAILED");
        log.setMessage("code=" + code);
        log.setCreatedAt(java.time.Instant.now());
        logs.save(log);
        return code >= 200 && code < 300;
    }
    
    public boolean deleteEventsBySummaryForCompletedTodo(String summary) {
        RadicaleClient client = new RadicaleClient(CalDavConfig.RADICALE_URL, CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD);
        int code = client.deleteEventsBySummary(summary);
        OperationLog log = new OperationLog();
        log.setOpType("DELETE_EVENT");
        log.setSummary(summary);
        log.setTargetType("EVENT");
        log.setStatus(code >= 200 && code < 300 ? "SUCCESS" : "FAILED");
        log.setMessage("code=" + code + "; reason=TODO_COMPLETED");
        log.setCreatedAt(java.time.Instant.now());
        logs.save(log);
        return code >= 200 && code < 300;
    }
    
    public boolean deleteEventsBySummaryForCompletedTodo(String summary, Long radicateConfigId) {
        ServiceConfig cfg = getConfig(radicateConfigId, ServiceType.RADICALE);
        String base = cfg != null ? cfg.getBaseUrl() : CalDavConfig.RADICALE_URL;
        String user = cfg != null ? cfg.getUsername() : CalDavConfig.RADICALE_USERNAME;
        String pass = cfg != null ? cfg.getPassword() : CalDavConfig.RADICALE_PASSWORD;
        RadicaleClient client = new RadicaleClient(base, user, pass);
        int code = client.deleteEventsBySummary(summary);
        OperationLog log = new OperationLog();
        log.setOpType("DELETE_EVENT");
        log.setSummary(summary);
        log.setTargetType("EVENT");
        log.setStatus(code >= 200 && code < 300 ? "SUCCESS" : "FAILED");
        log.setMessage("code=" + code + "; reason=TODO_COMPLETED");
        log.setCreatedAt(java.time.Instant.now());
        logs.save(log);
        return code >= 200 && code < 300;
    }
    
    public boolean eventSummaryExists(String summary) {
        RadicaleClient client = new RadicaleClient(CalDavConfig.RADICALE_URL, CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD);
        List<String> evs = client.listSummaries();
        return evs.contains(summary);
    }
    
    public boolean eventSummaryExists(String summary, Long radicateConfigId) {
        ServiceConfig cfg = getConfig(radicateConfigId, ServiceType.RADICALE);
        String base = cfg != null ? cfg.getBaseUrl() : CalDavConfig.RADICALE_URL;
        String user = cfg != null ? cfg.getUsername() : CalDavConfig.RADICALE_USERNAME;
        String pass = cfg != null ? cfg.getPassword() : CalDavConfig.RADICALE_PASSWORD;
        RadicaleClient client = new RadicaleClient(base, user, pass);
        List<String> evs = client.listSummaries();
        return evs.contains(summary);
    }
    
    
    private String uidFromSummary(String summary) {
        String src = summary == null ? "" : summary.trim().toLowerCase();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] dig = md.digest(src.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder("calsync-");
            for (byte b : dig) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return UUID.nameUUIDFromBytes(src.getBytes(StandardCharsets.UTF_8)).toString();
        }
    }
}
