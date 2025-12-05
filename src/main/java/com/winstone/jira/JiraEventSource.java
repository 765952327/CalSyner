package com.winstone.jira;

import com.winstone.sync.EventSpec;
import net.rcarz.jira.JiraClient;
import net.rcarz.jira.JiraException;
import net.rcarz.jira.SearchResult;
import net.rcarz.jira.issue.Issue;
import net.rcarz.jira.issue.IssueField;
import net.rcarz.jira.issue.Status;
import net.rcarz.jira.RestClient;
import net.rcarz.jira.BasicCredentials;
import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JiraEventSource {
    public List<EventSpec> fetch(String baseUrl, String email, String apiToken, String jql) {
        List<EventSpec> out = new ArrayList<>();
        try {
            RestClient rc = new RestClient(baseUrl, new BasicCredentials(email, apiToken));
            JiraClient client = new JiraClient(rc);
            SearchResult result = client.searchJql(jql);
            for (Issue issue : result.getIssues()) {
                String summary = issue.getSummary();
                String description = issue.getDescription();
                java.util.Date due = issue.getDueDate();
                Instant end = due != null ? due.toInstant() : issue.getCreated().toInstant();
                Instant start = end.minus(Duration.ofHours(1));
                EventSpec spec = new EventSpec(summary, start, end);
                spec.description = description;
                out.add(spec);
            }
        } catch (JiraException ignored) {}
        return out;
    }
}
