package com.jirasync.service.datasource;

import com.jirasync.domain.ServiceConfig;
import com.jirasync.domain.SyncTask;
import com.jirasync.web.dto.FieldMappingDTO;
import com.winstone.jira.JiraEventSource;
import com.winstone.sync.EventSpec;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class JiraDataSourceAdapter implements DataSourceAdapter {
    @Override
    public List<EventSpec> fetch(ServiceConfig srcCfg, SyncTask task, List<FieldMappingDTO> mappings) {
        String base = srcCfg != null ? srcCfg.getBaseUrl() : System.getenv().getOrDefault("JIRA_BASE_URL", "");
        String email = srcCfg != null ? srcCfg.getUsername() : System.getenv().getOrDefault("JIRA_EMAIL", "");
        String token = srcCfg != null ? srcCfg.getApiToken() : System.getenv().getOrDefault("JIRA_API_TOKEN", "");
        List<EventSpec> specs = new JiraEventSource().fetch(base, email, token, task.getJqlExpression());
        if (specs == null) specs = new ArrayList<>();
        if (mappings == null || mappings.isEmpty()) return specs;
        for (EventSpec s : specs) applyMappings(s, mappings);
        return specs;
    }

    private void applyMappings(EventSpec s, List<FieldMappingDTO> mappings) {
        for (FieldMappingDTO m : mappings) {
            String src = m.getJiraField();
            String dst = m.getRadicateField();
            if (dst == null) continue;
            if ("summary".equalsIgnoreCase(dst)) s.summary = pickString(s, src);
            else if ("description".equalsIgnoreCase(dst)) s.description = pickString(s, src);
            else if ("location".equalsIgnoreCase(dst)) s.location = pickString(s, src);
            else if ("start".equalsIgnoreCase(dst)) s.start = pickInstant(s, src, s.start);
            else if ("end".equalsIgnoreCase(dst)) s.end = pickInstant(s, src, s.end);
        }
    }

    private String pickString(EventSpec s, String src) {
        if (src == null) return null;
        switch (src.toLowerCase()) {
            case "summary": return s.summary;
            case "description": return s.description;
            case "location": return s.location;
            default: return s.summary;
        }
    }

    private Instant pickInstant(EventSpec s, String src, Instant fallback) {
        if (src == null) return fallback;
        switch (src.toLowerCase()) {
            case "start": return s.start;
            case "end": return s.end;
            default: return fallback;
        }
    }
}
