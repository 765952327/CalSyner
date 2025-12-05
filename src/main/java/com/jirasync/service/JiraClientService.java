package com.jirasync.service;

import com.winstone.jira.JiraEventSource;
import com.winstone.sync.EventSpec;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JiraClientService {
    private final com.jirasync.repository.ServiceConfigRepository serviceConfigs;

    public JiraClientService(com.jirasync.repository.ServiceConfigRepository serviceConfigs) {
        this.serviceConfigs = serviceConfigs;
    }

    public List<EventSpec> fetchByJql(Long jiraConfigId, String jql) {
        com.jirasync.domain.ServiceConfig cfg = null;
        if (jiraConfigId != null) {
            cfg = serviceConfigs.findById(jiraConfigId)
                    .filter(c -> "JIRA".equalsIgnoreCase(c.getServiceType()))
                    .orElse(null);
        }
        String baseUrl = cfg != null ? cfg.getBaseUrl() : System.getenv().getOrDefault("JIRA_BASE_URL", "");
        String email = cfg != null ? cfg.getUsername() : System.getenv().getOrDefault("JIRA_EMAIL", "");
        String token = cfg != null ? cfg.getApiToken() : System.getenv().getOrDefault("JIRA_API_TOKEN", "");
        return new JiraEventSource().fetch(baseUrl, email, token, jql);
    }
}
