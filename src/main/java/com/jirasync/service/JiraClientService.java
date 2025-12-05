package com.jirasync.service;

import com.winstone.jira.JiraEventSource;
import com.winstone.sync.EventSpec;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JiraClientService {
    public List<EventSpec> fetchByJql(String jql) {
        return new JiraEventSource().fetch();
    }
}
