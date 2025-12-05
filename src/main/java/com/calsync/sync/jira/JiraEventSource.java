package com.calsync.sync.jira;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.calsync.sync.EventSpec;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Jira 事件源：通过 JQL 从 Jira 搜索并抽取为统一事件模型。
 */
public class JiraEventSource {
    /**
     * 拉取事件。
     * @param baseUrl Jira 基础地址
     * @param email 用户邮箱
     * @param apiToken API Token
     * @param jql JQL 查询表达式
     */
    public List<EventSpec> fetch(String baseUrl, String email, String apiToken, String jql) {
        List<EventSpec> out = new ArrayList<>();
        try {
            String url = baseUrl + "/rest/api/3/search?jql=" + java.net.URLEncoder.encode(jql, "UTF-8");
            OkHttpClient client = new OkHttpClient();
            Request req = new Request.Builder().url(url)
                    .header("Authorization", Credentials.basic(email, apiToken))
                    .header("Accept", "application/json")
                    .get().build();
            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful()) return out;
                String json = resp.body().string();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(json);
                JsonNode issues = root.path("issues");
                if (issues.isArray()) {
                    for (JsonNode it : issues) {
                        JsonNode fields = it.path("fields");
                        String summary = fields.path("summary").asText(null);
                        String description = fields.path("description").isTextual() ? fields.path("description").asText() : null;
                        String dueStr = fields.path("duedate").asText(null);
                        Instant end;
                        if (dueStr != null && !dueStr.isEmpty()) {
                            LocalDate d = LocalDate.parse(dueStr);
                            end = d.atStartOfDay(ZoneId.systemDefault()).toInstant();
                        } else {
                            String created = fields.path("created").asText(null);
                            end = created != null ? Instant.parse(created) : Instant.now();
                        }
                        Instant start = end.minus(Duration.ofHours(1));
                        EventSpec spec = new EventSpec(summary, start, end);
                        spec.description = description;
                        out.add(spec);
                    }
                }
            }
        } catch (Exception ignored) {}
        return out;
    }
}
