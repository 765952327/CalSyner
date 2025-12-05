package com.jirasync.service;

import com.winstone.jira.JiraEventSource;
import com.winstone.sync.EventSpec;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JiraClientService {
    public List<EventSpec> fetchByJql(String jql) {
        String baseUrl = System.getenv().getOrDefault("JIRA_BASE_URL", "");
        String email = System.getenv().getOrDefault("JIRA_EMAIL", "");
        String token = System.getenv().getOrDefault("JIRA_API_TOKEN", "");
        return new JiraEventSource().fetch(baseUrl, email, token, jql);
    }
}
