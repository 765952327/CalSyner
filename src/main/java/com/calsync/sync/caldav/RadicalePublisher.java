package com.calsync.sync.caldav;

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

/**
 * Radicale 发布器：封装 CalDAV 资源的查询、替换与删除。
 */
public class RadicalePublisher {
//    private String ensureBase(String base) {
//        if (!base.endsWith("/")) return base + "/";
//        return base;
//    }
//
//    private List<String> listCollectionIcs(String baseUrl, String username, String password) {
//        String base = ensureBase(baseUrl);
//        OkHttpClient client = new OkHttpClient();
//        RequestBody body = RequestBody.create(MediaType.parse("text/xml"), "<propfind xmlns=\"DAV:\"><allprop/></propfind>");
//        Request req = new Request.Builder()
//                .url(base)
//                .header("Authorization", Credentials.basic(username, password))
//                .header("Depth", "1")
//                .method("PROPFIND", body)
//                .build();
//        List<String> result = new ArrayList<>();
//        try (Response resp = client.newCall(req).execute()) {
//            if (!resp.isSuccessful()) return result;
//            String xml = resp.body().string();
//            java.util.regex.Pattern p = java.util.regex.Pattern.compile("<href>(.*?)</href>", java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL);
//            java.util.regex.Matcher m = p.matcher(xml);
//            java.net.URL bu = new java.net.URL(base);
//            String origin = bu.getProtocol() + "://" + bu.getHost() + (bu.getPort() != -1 ? (":" + bu.getPort()) : "");
//            while (m.find()) {
//                String href = m.group(1).trim();
//                if (!href.endsWith(".ics")) continue;
//                String url = href.startsWith("http") ? href : origin + href;
//                result.add(url);
//            }
//        } catch (Exception ignored) {}
//        return result;
//    }
//
//    /** 列出仓库下所有 ICS 资源 URL（默认凭证） */
////    public List<String> listCollectionIcsUrls() {
////        return listCollectionIcs(CalDavConfig.RADICALE_URL, CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD);
////    }
//
//    /** 列出仓库下所有 ICS 资源 URL（指定凭证） */
//    public List<String> listCollectionIcsUrls(String baseUrl, String username, String password) {
//        return listCollectionIcs(baseUrl, username, password);
//    }
//
////    public List<String> listSummaries() {
////        return listSummaries(CalDavConfig.RADICALE_URL, CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD);
////    }
//
//    public List<String> listSummaries(String baseUrl, String username, String password) {
//        List<String> urls = listCollectionIcs(baseUrl, username, password);
//        OkHttpClient client = new OkHttpClient();
//        Set<String> summaries = new HashSet<>();
//        for (String url : urls) {
//            Request get = new Request.Builder()
//                    .url(url)
//                    .header("Authorization", Credentials.basic(username, password))
//                    .get().build();
//            try (Response resp = client.newCall(get).execute()) {
//                if (!resp.isSuccessful()) continue;
//                String ics = resp.body().string();
//                List<ICalendar> cals = Biweekly.parse(ics).all();
//                for (ICalendar cal : cals) {
//                    for (VEvent ev : cal.getEvents()) {
//                        Summary s = ev.getSummary();
//                        String v = s != null ? s.getValue() : null;
//                        if (v != null) summaries.add(v);
//                    }
//                    for (VTodo td : cal.getTodos()) {
//                        Summary s = td.getSummary();
//                        String v = s != null ? s.getValue() : null;
//                        if (v != null) summaries.add(v);
//                    }
//                }
//            } catch (Exception ignored) {}
//        }
//        return new ArrayList<>(summaries);
//    }
//
//    private String findIcsUrlBySummary(String summary, boolean todo, String baseUrl, String username, String password) {
//        List<String> urls = listCollectionIcs(baseUrl, username, password);
//        OkHttpClient client = new OkHttpClient();
//        for (String url : urls) {
//            Request get = new Request.Builder()
//                    .url(url)
//                    .header("Authorization", Credentials.basic(username, password))
//                    .get().build();
//            try (Response resp = client.newCall(get).execute()) {
//                if (!resp.isSuccessful()) continue;
//                String ics = resp.body().string();
//                List<ICalendar> cals = Biweekly.parse(ics).all();
//                for (ICalendar cal : cals) {
//                    if (!todo) {
//                        for (VEvent ev : cal.getEvents()) {
//                            Summary s = ev.getSummary();
//                            String v = s != null ? s.getValue() : null;
//                            if (summary.equals(v)) return url;
//                        }
//                    } else {
//                        for (VTodo td : cal.getTodos()) {
//                            Summary s = td.getSummary();
//                            String v = s != null ? s.getValue() : null;
//                            if (summary.equals(v)) return url;
//                        }
//                    }
//                }
//            } catch (Exception ignored) {}
//        }
//        return null;
//    }
//
//    public int deleteBySummary(String summary) {
//        return deleteBySummary(summary, CalDavConfig.RADICALE_URL, CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD);
//    }
//
//    public int deleteBySummary(String summary, String baseUrl, String username, String password) {
//        int code = 404;
//        String evUrl = findIcsUrlBySummary(summary, false, baseUrl, username, password);
//        String tdUrl = findIcsUrlBySummary(summary, true, baseUrl, username, password);
//        OkHttpClient client = new OkHttpClient();
//        try {
//            if (evUrl != null) {
//                Request del = new Request.Builder().url(evUrl)
//                        .header("Authorization", Credentials.basic(username, password))
//                        .delete().build();
//                try (Response resp = client.newCall(del).execute()) {
//                    code = resp.code();
//                }
//            }
//            if (tdUrl != null) {
//                Request del = new Request.Builder().url(tdUrl)
//                        .header("Authorization", Credentials.basic(username, password))
//                        .delete().build();
//                try (Response resp = client.newCall(del).execute()) {
//                    code = Math.max(code, resp.code());
//                }
//            }
//        } catch (Exception ignored) {}
//        return code;
//    }
//
//    public int replaceBySummary(String ics, String uid, String summary) {
//        return replaceBySummary(ics, uid, summary, CalDavConfig.RADICALE_URL, CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD);
//    }
//
//    public int replaceBySummary(String ics, String uid, String summary, String baseUrl, String username, String password) {
//        String url = findIcsUrlBySummary(summary, false, baseUrl, username, password);
//        if (url == null) {
//            String base = ensureBase(baseUrl);
//            url = base + uid + ".ics";
//        }
//        OkHttpClient client = new OkHttpClient();
//        Request put = new Request.Builder().url(url)
//                .header("Authorization", Credentials.basic(username, password))
//                .put(RequestBody.create(MediaType.parse("text/calendar"), ics)).build();
//        try (Response resp = client.newCall(put).execute()) {
//            return resp.code();
//        } catch (Exception e) {
//            return 500;
//        }
//    }
//
//    public int replaceTodoBySummary(String ics, String uid, String summary) {
//        return replaceTodoBySummary(ics, uid, summary, CalDavConfig.RADICALE_URL, CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD);
//    }
//
//    public int replaceTodoBySummary(String ics, String uid, String summary, String baseUrl, String username, String password) {
//        String url = findIcsUrlBySummary(summary, true, baseUrl, username, password);
//        if (url == null) {
//            String base = ensureBase(baseUrl);
//            url = base + uid + ".ics";
//        }
//        OkHttpClient client = new OkHttpClient();
//        Request put = new Request.Builder().url(url)
//                .header("Authorization", Credentials.basic(username, password))
//                .put(RequestBody.create(MediaType.parse("text/calendar"), ics)).build();
//        try (Response resp = client.newCall(put).execute()) {
//            return resp.code();
//        } catch (Exception e) {
//            return 500;
//        }
//    }
//
//    public int deleteEventsBySummary(String summary) {
//        return deleteEventsBySummary(summary, CalDavConfig.RADICALE_URL, CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD);
//    }
//
//    public int deleteEventsBySummary(String summary, String baseUrl, String username, String password) {
//        int code = 404;
//        String evUrl = findIcsUrlBySummary(summary, false, baseUrl, username, password);
//        OkHttpClient client = new OkHttpClient();
//        if (evUrl == null) return code;
//        Request del = new Request.Builder().url(evUrl)
//                .header("Authorization", Credentials.basic(username, password))
//                .delete().build();
//        try (Response resp = client.newCall(del).execute()) {
//            return resp.code();
//        } catch (Exception e) {
//            return 500;
//        }
//    }
//
//    /** 获取事件 ICS 文本（按摘要，默认凭证） */
//    public String getEventIcsBySummary(String summary) {
//        return getEventIcsBySummary(summary, CalDavConfig.RADICALE_URL, CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD);
//    }
//
//    /** 获取事件 ICS 文本（按摘要，指定凭证） */
//    public String getEventIcsBySummary(String summaryVal, String base, String user, String pass) {
//        for (String url : listCollectionIcs(base, user, pass)) {
//            OkHttpClient client = new OkHttpClient();
//            Request get = new Request.Builder()
//                    .url(url)
//                    .header("Authorization", Credentials.basic(user, pass))
//                    .get().build();
//            try (Response resp = client.newCall(get).execute()) {
//                if (!resp.isSuccessful()) continue;
//                String ics = resp.body().string();
//                List<ICalendar> cals = Biweekly.parse(ics).all();
//                for (ICalendar cal : cals) {
//                    for (VEvent ev : cal.getEvents()) {
//                        Summary s = ev.getSummary();
//                        String v = s != null ? s.getValue() : null;
//                        if (v != null && v.equals(summaryVal)) return ics;
//                    }
//                }
//            } catch (Exception ignored) {}
//        }
//        return null;
//    }
//
//    /** 获取待办 ICS 文本（按摘要，默认凭证） */
//    public String getTodoIcsBySummary(String summary) {
//        return getTodoIcsBySummary(summary, CalDavConfig.RADICALE_URL, CalDavConfig.RADICALE_USERNAME, CalDavConfig.RADICALE_PASSWORD);
//    }
//
//    /** 获取待办 ICS 文本（按摘要，指定凭证） */
//    public String getTodoIcsBySummary(String summaryVal, String base, String user, String pass) {
//        for (String url : listCollectionIcs(base, user, pass)) {
//            OkHttpClient client = new OkHttpClient();
//            Request get = new Request.Builder()
//                    .url(url)
//                    .header("Authorization", Credentials.basic(user, pass))
//                    .get().build();
//            try (Response resp = client.newCall(get).execute()) {
//                if (!resp.isSuccessful()) continue;
//                String ics = resp.body().string();
//                List<ICalendar> cals = Biweekly.parse(ics).all();
//                for (ICalendar cal : cals) {
//                    for (VTodo td : cal.getTodos()) {
//                        Summary s = td.getSummary();
//                        String v = s != null ? s.getValue() : null;
//                        if (v != null && v.equals(summaryVal)) return ics;
//                    }
//                }
//            } catch (Exception ignored) {}
//        }
//        return null;
//    }
//
//    /** 规范化 ICS 文本用于比较 */
//    public String normalize(String s) {
//        if (s == null) return "";
//        return s.replaceAll("\\r\\n", "\\n").replaceAll("\\s+", " ").trim();
//    }
}
