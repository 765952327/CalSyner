package com.calsync.service;

import com.calsync.sync.caldav.BiweeklyFormatter;
import com.calsync.sync.caldav.RadicalePublisher;
import com.calsync.sync.EventSpec;
import com.calsync.sync.caldav.CalDavConfig;
import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VTodo;
import biweekly.property.Summary;
import biweekly.property.Status;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.calsync.domain.OperationLog;
import com.calsync.repository.OperationLogRepository;
import com.calsync.domain.ServiceConfig;
import com.calsync.repository.ServiceConfigRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RadicateClientService implements com.calsync.sync.EventPublisher {
    private final BiweeklyFormatter formatter = new BiweeklyFormatter();
    private final RadicalePublisher publisher = new RadicalePublisher();
    private final OperationLogRepository logs;
    private final ServiceConfigRepository serviceConfigs;

    public void upsertEvents(List<EventSpec> specs) {
        Set<String> desired = new HashSet<>();
        for (EventSpec s : specs) desired.add(s.summary);
        List<String> existing = publisher.listSummaries();
        for (String ex : existing) {
            if (!desired.contains(ex)) publisher.deleteBySummary(ex);
        }
        for (EventSpec spec : specs) {
            String uid = UUID.randomUUID().toString();
            String ics = formatter.format(spec, uid);
            publisher.replaceBySummary(ics, uid, spec.summary);
            String todoIcs = formatter.formatTodo(spec, uid + "-todo");
            publisher.replaceTodoBySummary(todoIcs, uid + "-todo", spec.summary);
        }
    }

    @Override
    public void upsert(List<EventSpec> specs) {
        upsertEvents(specs);
    }

    public RadicateClientService(OperationLogRepository logs, ServiceConfigRepository serviceConfigs) {
        this.logs = logs;
        this.serviceConfigs = serviceConfigs;
    }

    private ServiceConfig getConfig(Long id, String expectType) {
        if (id == null) return null;
        return serviceConfigs.findById(id).filter(c -> expectType.equalsIgnoreCase(c.getServiceType())).orElse(null);
    }

    public List<RadicateSyncResult> upsertAndCollect(List<EventSpec> specs, Long recordId, Long taskId) {
        List<RadicateSyncResult> out = new ArrayList<>();
        Set<String> desired = new HashSet<>();
        for (EventSpec s : specs) desired.add(s.summary);
        List<String> existing = publisher.listSummaries();
        for (String ex : existing) {
            if (!desired.contains(ex)) publisher.deleteBySummary(ex);
        }
        for (EventSpec spec : specs) {
            String uid = UUID.randomUUID().toString();
            String ics = formatter.format(spec, uid);
            String prevEventIcs = publisher.getEventIcsBySummary(spec.summary);
            boolean existedEvent = prevEventIcs != null;
            boolean changedEvent = !existedEvent || !publisher.normalize(prevEventIcs).equals(publisher.normalize(ics));
            if (changedEvent) {
                int code = publisher.replaceBySummary(ics, uid, spec.summary);
                RadicateSyncResult ev = new RadicateSyncResult();
                ev.setSummary(spec.summary);
                ev.setUid(uid);
                ev.setCode(code);
                ev.setPayload(ics);
                ev.setTargetType("EVENT");
                out.add(ev);

                OperationLog logEv = new OperationLog();
                logEv.setOpType(existedEvent ? "UPDATE_EVENT" : "CREATE_EVENT");
                logEv.setSummary(spec.summary);
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
            String prevTodoIcs = publisher.getTodoIcsBySummary(spec.summary);
            boolean existedTodo = prevTodoIcs != null;
            boolean changedTodo = !existedTodo || !publisher.normalize(prevTodoIcs).equals(publisher.normalize(todoIcs));
            if (changedTodo) {
                int todoCode = publisher.replaceTodoBySummary(todoIcs, todoUid, spec.summary);
                RadicateSyncResult td = new RadicateSyncResult();
                td.setSummary(spec.summary);
                td.setUid(todoUid);
                td.setCode(todoCode);
                td.setPayload(todoIcs);
                td.setTargetType("TODO");
                out.add(td);

                OperationLog logTd = new OperationLog();
                logTd.setOpType(existedTodo ? "UPDATE_TODO" : "CREATE_TODO");
                logTd.setSummary(spec.summary);
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

    public List<RadicateSyncResult> upsertAndCollect(List<EventSpec> specs, Long recordId, Long taskId, Long radicateConfigId) {
        ServiceConfig cfg = getConfig(radicateConfigId, "RADICATE");
        String base = cfg != null ? cfg.getBaseUrl() : CalDavConfig.RADICALE_URL;
        String user = cfg != null ? cfg.getUsername() : CalDavConfig.RADICALE_USERNAME;
        String pass = cfg != null ? cfg.getPassword() : CalDavConfig.RADICALE_PASSWORD;
        List<RadicateSyncResult> out = new ArrayList<>();
        Set<String> desired = new HashSet<>();
        for (EventSpec s : specs) desired.add(s.summary);
        List<String> existing = publisher.listSummaries(base, user, pass);
        for (String ex : existing) {
            if (!desired.contains(ex)) publisher.deleteBySummary(ex, base, user, pass);
        }
        for (EventSpec spec : specs) {
            String uid = java.util.UUID.randomUUID().toString();
            String ics = formatter.format(spec, uid);
            String prevEventIcs = publisher.getEventIcsBySummary(spec.summary, base, user, pass);
            boolean existedEvent = prevEventIcs != null;
            boolean changedEvent = !existedEvent || !publisher.normalize(prevEventIcs).equals(publisher.normalize(ics));
            if (changedEvent) {
                int code = publisher.replaceBySummary(ics, uid, spec.summary, base, user, pass);
                RadicateSyncResult ev = new RadicateSyncResult();
                ev.setSummary(spec.summary);
                ev.setUid(uid);
                ev.setCode(code);
                ev.setPayload(ics);
                ev.setTargetType("EVENT");
                out.add(ev);

                OperationLog logEv = new OperationLog();
                logEv.setOpType(existedEvent ? "UPDATE_EVENT" : "CREATE_EVENT");
                logEv.setSummary(spec.summary);
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
            String prevTodoIcs = publisher.getTodoIcsBySummary(spec.summary, base, user, pass);
            boolean existedTodo = prevTodoIcs != null;
            boolean changedTodo = !existedTodo || !publisher.normalize(prevTodoIcs).equals(publisher.normalize(todoIcs));
            if (changedTodo) {
                int todoCode = publisher.replaceTodoBySummary(todoIcs, todoUid, spec.summary, base, user, pass);
                RadicateSyncResult td = new RadicateSyncResult();
                td.setSummary(spec.summary);
                td.setUid(todoUid);
                td.setCode(todoCode);
                td.setPayload(todoIcs);
                td.setTargetType("TODO");
                out.add(td);

                OperationLog logTd = new OperationLog();
                logTd.setOpType(existedTodo ? "UPDATE_TODO" : "CREATE_TODO");
                logTd.setSummary(spec.summary);
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
        List<String> urls = publisher.listCollectionIcsUrls();
        OkHttpClient client = new OkHttpClient();
        Set<String> completed = new HashSet<>();
        for (String url : urls) {
            Request get = new Request.Builder()
                    .url(url)
                    .header("Authorization", Credentials.basic(CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD))
                    .get()
                    .build();
            try (Response resp = client.newCall(get).execute()) {
                if (!resp.isSuccessful()) continue;
                String ics = resp.body().string();
                List<ICalendar> cals = Biweekly.parse(ics).all();
                for (ICalendar cal : cals) {
                    for (VTodo td : cal.getTodos()) {
                        Status st = td.getStatus();
                        Summary s = td.getSummary();
                        String v = s != null ? s.getValue() : null;
                        if (st != null && "COMPLETED".equalsIgnoreCase(st.getValue()) && v != null) completed.add(v);
                    }
                }
            } catch (Exception ignored) {}
        }
        return new ArrayList<>(completed);
    }

    public List<String> listCompletedTodoSummaries(Long radicateConfigId) {
        ServiceConfig cfg = getConfig(radicateConfigId, "RADICATE");
        String base = cfg != null ? cfg.getBaseUrl() : CalDavConfig.RADICALE_URL;
        String user = cfg != null ? cfg.getUsername() : CalDavConfig.RADICALE_USERNAME;
        String pass = cfg != null ? cfg.getPassword() : CalDavConfig.RADICALE_PASSWORD;
        List<String> urls = publisher.listCollectionIcsUrls(base, user, pass);
        OkHttpClient client = new OkHttpClient();
        Set<String> completed = new HashSet<>();
        for (String url : urls) {
            Request get = new Request.Builder()
                    .url(url)
                    .header("Authorization", Credentials.basic(user, pass))
                    .get()
                    .build();
            try (Response resp = client.newCall(get).execute()) {
                if (!resp.isSuccessful()) continue;
                String ics = resp.body().string();
                List<ICalendar> cals = Biweekly.parse(ics).all();
                for (ICalendar cal : cals) {
                    for (VTodo td : cal.getTodos()) {
                        Status st = td.getStatus();
                        Summary s = td.getSummary();
                        String v = s != null ? s.getValue() : null;
                        if (st != null && "COMPLETED".equalsIgnoreCase(st.getValue()) && v != null) completed.add(v);
                    }
                }
            } catch (Exception ignored) {}
        }
        return new ArrayList<>(completed);
    }

    public boolean deleteEventsBySummary(String summary) {
        int code = publisher.deleteEventsBySummary(summary);
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
        ServiceConfig cfg = getConfig(radicateConfigId, "RADICATE");
        String base = cfg != null ? cfg.getBaseUrl() : CalDavConfig.RADICALE_URL;
        String user = cfg != null ? cfg.getUsername() : CalDavConfig.RADICALE_USERNAME;
        String pass = cfg != null ? cfg.getPassword() : CalDavConfig.RADICALE_PASSWORD;
        int code = publisher.deleteEventsBySummary(summary, base, user, pass);
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
        int code = publisher.deleteEventsBySummary(summary);
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
        ServiceConfig cfg = getConfig(radicateConfigId, "RADICATE");
        String base = cfg != null ? cfg.getBaseUrl() : CalDavConfig.RADICALE_URL;
        String user = cfg != null ? cfg.getUsername() : CalDavConfig.RADICALE_USERNAME;
        String pass = cfg != null ? cfg.getPassword() : CalDavConfig.RADICALE_PASSWORD;
        int code = publisher.deleteEventsBySummary(summary, base, user, pass);
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
        List<String> evs = publisher.listSummaries();
        return evs.contains(summary);
    }

    public boolean eventSummaryExists(String summary, Long radicateConfigId) {
        ServiceConfig cfg = getConfig(radicateConfigId, "RADICATE");
        String base = cfg != null ? cfg.getBaseUrl() : CalDavConfig.RADICALE_URL;
        String user = cfg != null ? cfg.getUsername() : CalDavConfig.RADICALE_USERNAME;
        String pass = cfg != null ? cfg.getPassword() : CalDavConfig.RADICALE_PASSWORD;
        List<String> evs = publisher.listSummaries(base, user, pass);
        return evs.contains(summary);
    }

    
}
