package com.calsync.service.datasource;

import com.calsync.domain.ServiceConfig;
import com.calsync.domain.SyncTask;
import com.calsync.web.dto.FieldMappingDTO;
import com.calsync.sync.jira.JiraEventSource;
import com.calsync.sync.EventSpec;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import com.calsync.service.mapper.FieldMapper;

/**
 * Jira 数据源适配器：基于 JQL 拉取 Jira 事件并应用字段映射。
 */
public class JiraDataSourceAdapter implements DataSourceAdapter {
    @Override
    public List<EventSpec> fetch(ServiceConfig srcCfg, SyncTask task, List<FieldMappingDTO> mappings) {
        String base = srcCfg != null ? srcCfg.getBaseUrl() : System.getenv().getOrDefault("JIRA_BASE_URL", "");
        String email = srcCfg != null ? srcCfg.getUsername() : System.getenv().getOrDefault("JIRA_EMAIL", "");
        String token = srcCfg != null ? srcCfg.getApiToken() : System.getenv().getOrDefault("JIRA_API_TOKEN", "");
        List<EventSpec> specs = new JiraEventSource().fetch(base, email, token, task.getJqlExpression());
        if (specs == null) specs = new ArrayList<>();
        if (mappings == null || mappings.isEmpty()) return specs;
        for (EventSpec s : specs) FieldMapper.apply(s, mappings);
        return specs;
    }
}
