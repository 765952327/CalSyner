package com.calsync.sync.radicale;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RadicaleClient {
    private final static OkHttpClient client = new OkHttpClient();
    private final String baseUrl;
    private final String username;
    private final String password;
    
    
    public RadicaleClient(String baseUrl, String username, String password) {
        this.baseUrl = ensureBase(baseUrl);
        this.username = username;
        this.password = password;
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
}
