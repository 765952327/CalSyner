package com.calsync.sync.jira;

import com.calsync.domain.SyncTask;
import com.calsync.service.ServiceConfigService;
import com.calsync.service.SyncTaskService;
import com.calsync.sync.Event;
import com.calsync.sync.EventSource;
import com.calsync.sync.ParamsSource;
import java.util.ArrayList;
import java.util.List;
import net.rcarz.jiraclient.JiraException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JiraManger
 */
@Component
public class JiraManger extends JiraClientService implements EventSource, ParamsSource<JiraParam> {
    @Autowired
    private SyncTaskService syncTaskService;
    @Autowired
    private ServiceConfigService serviceConfigService;
    
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
    public List<Event> fetch(Long taskId) {
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
