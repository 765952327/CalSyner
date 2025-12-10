package com.calsync.sync.jira;

import com.calsync.domain.ServiceConfig;
import com.calsync.service.ServiceConfigService;
import com.calsync.sync.ClientService;
import java.util.HashMap;
import java.util.Map;
import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.JiraClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JiraClientService implements ClientService<JiraClientWrap> {
    private static final Map<Long, JiraClientWrap> clientMap = new HashMap<>();
    @Autowired
    private ServiceConfigService serviceConfigService;
    
    @Override
    public boolean test(Long serviceId) {
        try {
            JiraClientWrap client = getClient(serviceId);
            JiraClient jiraClient = client.getJiraClient();
            jiraClient.getIssueTypes();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public JiraClientWrap getClient(Long serviceId) {
        if (clientMap.containsKey(serviceId)) {
            return clientMap.get(serviceId);
        }
        ServiceConfig config = serviceConfigService.getConfig(serviceId);
        JiraClient client = new JiraClient(config.getBaseUrl(),
                new BasicCredentials(config.getUsername(), config.getPassword()));
        try {
            JiraClientWrap clientWrap = new JiraClientWrap(client);
            clientMap.put(serviceId, clientWrap);
            return clientWrap;
        } catch (Exception e) {
            throw new RuntimeException("get client error", e);
        }
    }
}
