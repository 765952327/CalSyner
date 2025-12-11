package com.calsync.sync.radicale;

import biweekly.Biweekly;
import biweekly.ICalendar;
import com.calsync.util.ServiceUtil;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;

public class RadicaleClient {
    private final static OkHttpClient client = new OkHttpClient();
    private final String baseUrl;
    private final String username;
    private final String password;
    
    public RadicaleClient(String baseUrl, String username, String password) {
        this.baseUrl = ServiceUtil.ensureUrl(baseUrl);
        this.username = username;
        this.password = password;
    }
    
    
    public boolean ping() {
        String body = getIcs(baseUrl);
        return body != null;
    }
    
    public List<ICalendar> queryAll() {
        List<String> urls = queryAllUrl();
        List<ICalendar> out = new ArrayList<>();
        for (String url : urls) {
            List<ICalendar> iCalendars = get(url);
            if (!iCalendars.isEmpty()) {
                out.addAll(iCalendars);
            }
        }
        return out;
    }
    
    public int upsertByUid(String uid, ICalendar iCalendar) {
        String ics = Biweekly.write(iCalendar).go();
        String url = baseUrl + uid + ".ics";
        return putCalendar(url, ics);
    }
    
    public String getByUid(String uid) {
        String url = baseUrl + uid + ".ics";
        return getIcs(url);
    }
    
    public int deleteByUid(String uid) {
        String url = baseUrl + uid + ".ics";
        return delete(url);
    }
    
    private List<String> queryAllUrl() {
        String xml = propfind(baseUrl);
        List<String> result = new ArrayList<>();
        if (xml == null) return result;
        Pattern p = Pattern.compile("<(?:\\w+:)?href>(.*?)</(?:\\w+:)?href>", java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL);
        Matcher m = p.matcher(xml);
        try {
            URL bu = new URL(baseUrl);
            String origin = bu.getProtocol() + "://" + bu.getHost() + (bu.getPort() != -1 ? (":" + bu.getPort()) : "");
            while (m.find()) {
                String href = m.group(1).trim();
                if (!href.endsWith(".ics")) continue;
                String url = href.startsWith("http") ? href : origin + href;
                result.add(url);
            }
        } catch (Exception ignored) {
        }
        return result;
    }
    
    private Request.Builder auth(Request.Builder b) {
        return b.header("Authorization", Credentials.basic(username, password));
    }
    
    private List<ICalendar> get(String url) {
        String ics = getIcs(url);
        if (StringUtils.isBlank(ics)) {
            return new ArrayList<>();
        }
        return Biweekly.parse(ics).all();
    }
    
    private String getIcs(String url) {
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
