package com.calsync.service;

import com.calsync.repository.ServiceConfigRepository;
import com.calsync.sync.jira.JiraManger;
import com.calsync.sync.EventSpec;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JiraClientService {
    private final ServiceConfigRepository serviceConfigs;

    public JiraClientService(ServiceConfigRepository serviceConfigs) {
        this.serviceConfigs = serviceConfigs;
    }

    public List<EventSpec> fetchByJql(Long jiraConfigId, String jql) {
        com.calsync.domain.ServiceConfig cfg = null;
        if (jiraConfigId != null) {
            cfg = serviceConfigs.findById(jiraConfigId)
                    .filter(c -> "JIRA".equalsIgnoreCase(c.getServiceType()))
                    .orElse(null);
        }
        String baseUrl = cfg != null ? cfg.getBaseUrl() : System.getenv().getOrDefault("JIRA_BASE_URL", "");
        String email = cfg != null ? cfg.getUsername() : System.getenv().getOrDefault("JIRA_EMAIL", "");
        String token = cfg != null ? cfg.getApiToken() : System.getenv().getOrDefault("JIRA_API_TOKEN", "");
        return new JiraManger().fetch(baseUrl, email, token, jql);
    }
}
