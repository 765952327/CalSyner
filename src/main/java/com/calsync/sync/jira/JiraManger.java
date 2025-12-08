package com.calsync.sync.jira;

import com.calsync.domain.ServiceConfig;
import com.calsync.domain.SyncTask;
import com.calsync.service.JiraClientService;
import com.calsync.service.ServiceConfigService;
import com.calsync.service.SyncTaskService;
import com.calsync.sync.EventSource;
import com.calsync.sync.EventSpec;
import com.calsync.sync.Param;
import com.calsync.sync.ParamsSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JiraManger
 */
@Component
public class JiraManger implements EventSource {
    private static final Map<Long, JiraClient> clientMap = new HashMap<>();
    @Autowired
    private SyncTaskService syncTaskService;
    @Autowired
    private ServiceConfigService serviceConfigService;
    
    
    private JiraClient getClient(Long id) {
        SyncTask task = syncTaskService.getTask(id);
        if (task == null) {
            return null;
        }
        Long jiraConfigId = task.getJiraConfigId();
        if (clientMap.containsKey(jiraConfigId)){
            return clientMap.get(jiraConfigId);
        }
        ServiceConfig config = serviceConfigService.getConfig(jiraConfigId);
        JiraClient client = new JiraClient(config.getBaseUrl(),
                new BasicCredentials(config.getUsername(), config.getPassword()));
        clientMap.put(jiraConfigId,client);
        return client;
    }
    
    private IssuesWrap queryIssues(SyncTask task) throws JiraException {
        String jql = task.getJqlExpression();
        Long id = task.getId();
        JiraClient client = getClient(id);
        if (client == null){
            throw new JiraException("no client");
        }
        Issue.SearchResult result = client.searchIssues(jql);
        return new IssuesWrap(result.issues);
    }
    
    
    
    /**
     * 拉取事件。
     * @param baseUrl  JiraManger 基础地址
     * @param email    用户邮箱
     * @param apiToken API Token
     * @param jql      JQL 查询表达式
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
                        String key = it.path("key").asText(null);
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
                        spec.setSummary(description);
                        spec.setExternalId(key);
                        String created = fields.path("created").asText(null);
                        String updated = fields.path("updated").asText(null);
                        spec.setCreatedAt(created != null ? Instant.parse(created) : null);
                        spec.setUpdatedAt(updated != null ? Instant.parse(updated) : null);
                        spec.setUrl(key != null ? (baseUrl + "/browse/" + key) : null);
                        JsonNode pri = fields.path("priority").path("name");
                        if (pri.isTextual()) {
                            String p = pri.asText();
                            spec.setPriority("Highest".equalsIgnoreCase(p) ? 5 : ("High".equalsIgnoreCase(p) ? 4 : ("Medium".equalsIgnoreCase(p) ? 3 : ("Low".equalsIgnoreCase(p) ? 2 : 1))));
                        }
                        out.add(spec);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return out;
    }
    
    
    @Override
    public List<EventSpec> fetch(Long taskId) {
        try {
            IssuesWrap issuesWrap = queryIssues(syncTaskService.getTask(taskId));
            List<Param> params = issuesWrap.getParams();
        } catch (JiraException e) {
            throw new RuntimeException(e);
        }
        return java.util.Collections.emptyList();
    }
    
}
