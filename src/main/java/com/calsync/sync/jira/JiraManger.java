package com.calsync.sync.jira;

import com.calsync.domain.ServiceConfig;
import com.calsync.domain.SyncTask;
import com.calsync.service.ServiceConfigService;
import com.calsync.service.SyncTaskService;
import com.calsync.sync.EventSource;
import com.calsync.sync.EventSpec;
import com.calsync.sync.ParamsSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JiraManger
 */
@Component
public class JiraManger implements EventSource, ParamsSource<JiraParam> {
    private static final Map<Long, JiraClientWrap> clientMap = new HashMap<>();
    @Autowired
    private SyncTaskService syncTaskService;
    @Autowired
    private ServiceConfigService serviceConfigService;
    
    
    private JiraClientWrap getClient(Long id) throws JiraException {
        SyncTask task = syncTaskService.getTask(id);
        if (task == null) {
            return null;
        }
        Long jiraId = task.getJiraConfigId();
        if (clientMap.containsKey(jiraId)) {
            return clientMap.get(jiraId);
        }
        ServiceConfig config = serviceConfigService.getConfig(jiraId);
        JiraClient client = new JiraClient(config.getBaseUrl(),
                new BasicCredentials(config.getUsername(), config.getPassword()));
        JiraClientWrap clientWrap = new JiraClientWrap(client);
        clientMap.put(jiraId, clientWrap);
        return clientWrap;
    }
    
    private IssuesWrap queryIssues(SyncTask task) throws JiraException {
        String jql = task.getJqlExpression();
        Long id = task.getId();
        JiraClientWrap client = getClient(id);
        if (client == null) {
            throw new JiraException("no client");
        }
        return client.searchIssues(jql);
    }
    
    
    @Override
    public List<EventSpec> fetch(Long taskId) {
        try {
            SyncTask task = syncTaskService.getTask(taskId);
            if (task == null) {
                return new ArrayList<>();
            }
            IssuesWrap issuesWrap = queryIssues(task);
        } catch (JiraException e) {
            throw new RuntimeException(e);
        }
        return new ArrayList<>();
    }
    
    @Override
    public List<JiraParam> getParams(Long taskId) {
        try {
            JiraClientWrap client = getClient(taskId);
            if (client == null) {
                throw new JiraException("no client");
            }
            return client.getParams();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
