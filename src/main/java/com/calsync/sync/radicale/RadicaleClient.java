package com.calsync.sync.radicale;

import com.calsync.sync.caldav.CalDavConfig;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.component.VTodo;
import biweekly.property.Status;
import biweekly.property.Summary;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RadicaleClient {
    private final String baseUrl;
    private final String username;
    private final String password;
    private final OkHttpClient client = new OkHttpClient();

    public RadicaleClient(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl != null ? baseUrl : CalDavConfig.RADICALE_URL;
        this.username = username != null ? username : CalDavConfig.RADICALE_USERNAME;
        this.password = password != null ? password : CalDavConfig.RADICALE_PASSWORD;
    }

    public List<String> listSummaries() {
        List<String> urls = listCollectionIcsUrls();
        Set<String> summaries = new HashSet<>();
        for (String url : urls) {
            String ics = get(url);
            if (ics == null) continue;
                List<ICalendar> cals = Biweekly.parse(ics).all();
                for (ICalendar cal : cals) {
                    for (VEvent ev : cal.getEvents()) {
                        Summary s = ev.getSummary();
                        String v = s != null ? s.getValue() : null;
                        if (v != null) summaries.add(v);
                    }
                    for (VTodo td : cal.getTodos()) {
                        Summary s = td.getSummary();
                        String v = s != null ? s.getValue() : null;
                        if (v != null) summaries.add(v);
                    }
                }
        }
        return new ArrayList<>(summaries);
    }

    public int deleteBySummary(String summary) {
        int code = 404;
        String evUrl = findIcsUrlBySummary(summary, false);
        String tdUrl = findIcsUrlBySummary(summary, true);
        if (evUrl != null) code = delete(evUrl);
        if (tdUrl != null) code = Math.max(code, delete(tdUrl));
        return code;
    }

    public int deleteEventsBySummary(String summary) {
        int code = 404;
        String evUrl = findIcsUrlBySummary(summary, false);
        if (evUrl == null) return code;
        return delete(evUrl);
    }

    public int replaceBySummary(String ics, String uid, String summary) {
        String url = findIcsUrlBySummary(summary, false);
        if (url == null) {
            String base = ensureBase(baseUrl);
            url = base + uid + ".ics";
        }
        return putCalendar(url, ics);
    }

    public int replaceTodoBySummary(String ics, String uid, String summary) {
        String url = findIcsUrlBySummary(summary, true);
        if (url == null) {
            String base = ensureBase(baseUrl);
            url = base + uid + ".ics";
        }
        return putCalendar(url, ics);
    }

    public String getEventIcsBySummary(String summary) {
        for (String url : listCollectionIcsUrls()) {
            String ics = get(url);
            if (ics == null) continue;
                List<ICalendar> cals = Biweekly.parse(ics).all();
                for (ICalendar cal : cals) {
                    for (VEvent ev : cal.getEvents()) {
                        Summary s = ev.getSummary();
                        String v = s != null ? s.getValue() : null;
                        if (v != null && v.equals(summary)) return ics;
                    }
                }
        }
        return null;
    }

    public String getTodoIcsBySummary(String summary) {
        for (String url : listCollectionIcsUrls()) {
            String ics = get(url);
            if (ics == null) continue;
                List<ICalendar> cals = Biweekly.parse(ics).all();
                for (ICalendar cal : cals) {
                    for (VTodo td : cal.getTodos()) {
                        Summary s = td.getSummary();
                        String v = s != null ? s.getValue() : null;
                        if (v != null && v.equals(summary)) return ics;
                    }
                }
        }
        return null;
    }

    public List<String> listCollectionIcsUrls() {
        String base = ensureBase(baseUrl);
        String xml = propfind(base);
        List<String> result = new ArrayList<>();
        if (xml == null) return result;
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
        return result;
    }

    public String normalize(String s) {
        if (s == null) return "";
        return s.replaceAll("\\r\\n", "\\n").replaceAll("\\s+", " ").trim();
    }

    public List<String> listCompletedTodoSummaries() {
        List<String> urls = listCollectionIcsUrls();
        Set<String> completed = new HashSet<>();
        for (String url : urls) {
            String ics = get(url);
            if (ics == null) continue;
                List<ICalendar> cals = Biweekly.parse(ics).all();
                for (ICalendar cal : cals) {
                    for (VTodo td : cal.getTodos()) {
                        Status st = td.getStatus();
                        Summary s = td.getSummary();
                        String v = s != null ? s.getValue() : null;
                        if (st != null && "COMPLETED".equalsIgnoreCase(st.getValue()) && v != null) completed.add(v);
                    }
                }
        }
        return new ArrayList<>(completed);
    }

    public boolean ping() {
        String base = ensureBase(baseUrl);
        String body = get(base);
        return body != null;
    }

    private String ensureBase(String base) {
        if (!base.endsWith("/")) return base + "/";
        return base;
    }

    private String findIcsUrlBySummary(String summary, boolean todo) {
        List<String> urls = listCollectionIcsUrls();
        for (String url : urls) {
            String ics = get(url);
            if (ics == null) continue;
                List<ICalendar> cals = Biweekly.parse(ics).all();
                for (ICalendar cal : cals) {
                    if (!todo) {
                        for (VEvent ev : cal.getEvents()) {
                            Summary s = ev.getSummary();
                            String v = s != null ? s.getValue() : null;
                            if (summary.equals(v)) return url;
                        }
                    } else {
                        for (VTodo td : cal.getTodos()) {
                            Summary s = td.getSummary();
                            String v = s != null ? s.getValue() : null;
                            if (summary.equals(v)) return url;
                        }
                    }
                }
        }
        return null;
    }

    private Request.Builder auth(Request.Builder b) {
        return b.header("Authorization", Credentials.basic(username, password));
    }

    private String get(String url) {
        Request req = auth(new Request.Builder().url(url).get()).build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) return null;
            return resp.body() != null ? resp.body().string() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String propfind(String base) {
        RequestBody body = RequestBody.create(MediaType.parse("text/xml"), "<propfind xmlns=\"DAV:\"><allprop/></propfind>");
        Request req = auth(new Request.Builder().url(base).header("Depth", "1").method("PROPFIND", body)).build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) return null;
            return resp.body() != null ? resp.body().string() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private int putCalendar(String url, String ics) {
        Request req = auth(new Request.Builder().url(url).put(RequestBody.create(MediaType.parse("text/calendar"), ics))).build();
        try (Response resp = client.newCall(req).execute()) {
            return resp.code();
        } catch (Exception e) {
            return 500;
        }
    }

    private int delete(String url) {
        Request req = auth(new Request.Builder().url(url).delete()).build();
        try (Response resp = client.newCall(req).execute()) {
            return resp.code();
        } catch (Exception e) {
            return 500;
        }
    }
}
