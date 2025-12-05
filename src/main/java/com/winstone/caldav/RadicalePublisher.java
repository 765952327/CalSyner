package com.winstone.caldav;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.component.VTodo;
import biweekly.property.Summary;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RadicalePublisher {
    private String ensureBase(String base) {
        if (!base.endsWith("/")) return base + "/";
        return base;
    }

    private List<String> listCollectionIcs() {
        String base = ensureBase(CalDavConfig.RADICALE_URL);
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("text/xml"), "<propfind xmlns=\"DAV:\"><allprop/></propfind>");
        Request req = new Request.Builder()
                .url(base)
                .header("Authorization", Credentials.basic(CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD))
                .header("Depth", "1")
                .method("PROPFIND", body)
                .build();
        List<String> result = new ArrayList<>();
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

    public List<String> listSummaries() {
        List<String> urls = listCollectionIcs();
        OkHttpClient client = new OkHttpClient();
        Set<String> summaries = new HashSet<>();
        for (String url : urls) {
            Request get = new Request.Builder()
                    .url(url)
                    .header("Authorization", Credentials.basic(CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD))
                    .get().build();
            try (Response resp = client.newCall(get).execute()) {
                if (!resp.isSuccessful()) continue;
                String ics = resp.body().string();
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
            } catch (Exception ignored) {}
        }
        return new ArrayList<>(summaries);
    }

    private String findIcsUrlBySummary(String summary, boolean todo) {
        List<String> urls = listCollectionIcs();
        OkHttpClient client = new OkHttpClient();
        for (String url : urls) {
            Request get = new Request.Builder()
                    .url(url)
                    .header("Authorization", Credentials.basic(CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD))
                    .get().build();
            try (Response resp = client.newCall(get).execute()) {
                if (!resp.isSuccessful()) continue;
                String ics = resp.body().string();
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
            } catch (Exception ignored) {}
        }
        return null;
    }

    public int deleteBySummary(String summary) {
        int code = 404;
        String evUrl = findIcsUrlBySummary(summary, false);
        String tdUrl = findIcsUrlBySummary(summary, true);
        OkHttpClient client = new OkHttpClient();
        try {
            if (evUrl != null) {
                Request del = new Request.Builder().url(evUrl)
                        .header("Authorization", Credentials.basic(CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD))
                        .delete().build();
                try (Response resp = client.newCall(del).execute()) {
                    code = resp.code();
                }
            }
            if (tdUrl != null) {
                Request del = new Request.Builder().url(tdUrl)
                        .header("Authorization", Credentials.basic(CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD))
                        .delete().build();
                try (Response resp = client.newCall(del).execute()) {
                    code = Math.max(code, resp.code());
                }
            }
        } catch (Exception ignored) {}
        return code;
    }

    public int replaceBySummary(String ics, String uid, String summary) {
        String url = findIcsUrlBySummary(summary, false);
        if (url == null) {
            String base = ensureBase(CalDavConfig.RADICALE_URL);
            url = base + uid + ".ics";
        }
        OkHttpClient client = new OkHttpClient();
        Request put = new Request.Builder().url(url)
                .header("Authorization", Credentials.basic(CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD))
                .put(RequestBody.create(MediaType.parse("text/calendar"), ics)).build();
        try (Response resp = client.newCall(put).execute()) {
            return resp.code();
        } catch (Exception e) {
            return 500;
        }
    }

    public int replaceTodoBySummary(String ics, String uid, String summary) {
        String url = findIcsUrlBySummary(summary, true);
        if (url == null) {
            String base = ensureBase(CalDavConfig.RADICALE_URL);
            url = base + uid + ".ics";
        }
        OkHttpClient client = new OkHttpClient();
        Request put = new Request.Builder().url(url)
                .header("Authorization", Credentials.basic(CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD))
                .put(RequestBody.create(MediaType.parse("text/calendar"), ics)).build();
        try (Response resp = client.newCall(put).execute()) {
            return resp.code();
        } catch (Exception e) {
            return 500;
        }
    }

    public int deleteEventsBySummary(String summary) {
        int code = 404;
        String evUrl = findIcsUrlBySummary(summary, false);
        OkHttpClient client = new OkHttpClient();
        if (evUrl == null) return code;
        Request del = new Request.Builder().url(evUrl)
                .header("Authorization", Credentials.basic(CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD))
                .delete().build();
        try (Response resp = client.newCall(del).execute()) {
            return resp.code();
        } catch (Exception e) {
            return 500;
        }
    }
}
