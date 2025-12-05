package com.jirasync.service;

import com.winstone.caldav.BiweeklyFormatter;
import com.winstone.caldav.RadicalePublisher;
import com.winstone.sync.EventSpec;
import com.winstone.caldav.CalDavConfig;
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
import com.jirasync.domain.OperationLog;
import com.jirasync.repository.OperationLogRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RadicateClientService {
    private final BiweeklyFormatter formatter = new BiweeklyFormatter();
    private final RadicalePublisher publisher = new RadicalePublisher();
    private final OperationLogRepository logs;

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

    public RadicateClientService(OperationLogRepository logs) { this.logs = logs; }

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
            String prevEventIcs = getEventIcsBySummary(spec.summary);
            boolean existedEvent = prevEventIcs != null;
            boolean changedEvent = !existedEvent || !normalize(prevEventIcs).equals(normalize(ics));
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
            String prevTodoIcs = getTodoIcsBySummary(spec.summary);
            boolean existedTodo = prevTodoIcs != null;
            boolean changedTodo = !existedTodo || !normalize(prevTodoIcs).equals(normalize(todoIcs));
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

    private List<String> listCollectionIcs() {
        String base = CalDavConfig.RADICALE_URL;
        if (!base.endsWith("/")) base = base + "/";
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("text/xml"), "<propfind xmlns=\"DAV:\"><allprop/></propfind>");
        Request req = new Request.Builder()
                .url(base)
                .header("Authorization", Credentials.basic(CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD))
                .header("Depth", "1")
                .method("PROPFIND", body)
                .build();
        java.util.List<String> result = new java.util.ArrayList<>();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) return result;
            String xml = resp.body().string();
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("<href>(.*?)</href>", java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher m = p.matcher(xml);
            java.net.URL bu = new java.net.URL(base);
            String origin = bu.getProtocol() + "://" + bu.getHost() + (bu.getPort() != -1 ? (":" + bu.getPort()) : "");
            while (m.find()) {
                String href = m.group(1).trim();
                if (!href.endsWith(".ics")) continue;
                String url = href.startsWith("http") ? href : origin + href;
                result.add(url);
            }
        } catch (Exception ignored) {}
        return result;
    }

    public List<String> listCompletedTodoSummaries() {
        List<String> urls = listCollectionIcs();
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

    public boolean eventSummaryExists(String summary) {
        List<String> evs = publisher.listSummaries();
        return evs.contains(summary);
    }

    private String getEventIcsBySummary(String summaryVal) {
        for (String url : listCollectionIcs()) {
            OkHttpClient client = new OkHttpClient();
            Request get = new Request.Builder()
                    .url(url)
                    .header("Authorization", Credentials.basic(CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD))
                    .get().build();
            try (Response resp = client.newCall(get).execute()) {
                if (!resp.isSuccessful()) continue;
                String ics = resp.body().string();
                List<ICalendar> cals = Biweekly.parse(ics).all();
                for (ICalendar cal : cals) {
                    for (biweekly.component.VEvent ev : cal.getEvents()) {
                        Summary s = ev.getSummary();
                        String v = s != null ? s.getValue() : null;
                        if (v != null && v.equals(summaryVal)) return ics;
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String getTodoIcsBySummary(String summaryVal) {
        for (String url : listCollectionIcs()) {
            OkHttpClient client = new OkHttpClient();
            Request get = new Request.Builder()
                    .url(url)
                    .header("Authorization", Credentials.basic(CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD))
                    .get().build();
            try (Response resp = client.newCall(get).execute()) {
                if (!resp.isSuccessful()) continue;
                String ics = resp.body().string();
                List<ICalendar> cals = Biweekly.parse(ics).all();
                for (ICalendar cal : cals) {
                    for (VTodo td : cal.getTodos()) {
                        Summary s = td.getSummary();
                        String v = s != null ? s.getValue() : null;
                        if (v != null && v.equals(summaryVal)) return ics;
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.replaceAll("\\r\\n", "\\n").replaceAll("\\s+", " ").trim();
    }
}
