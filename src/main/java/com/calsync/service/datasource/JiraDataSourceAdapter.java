package com.calsync.service.datasource;

import com.calsync.domain.ServiceConfig;
import com.calsync.domain.SyncTask;
import com.calsync.web.dto.FieldMappingDTO;
import com.calsync.sync.Event;
import java.util.ArrayList;
import java.util.List;

/**
 * JiraManger 数据源适配器：基于 JQL 拉取 JiraManger 事件并应用字段映射。
 */
public class JiraDataSourceAdapter implements DataSourceAdapter {
    @Override
    public List<Event> fetch(ServiceConfig srcCfg, SyncTask task, List<FieldMappingDTO> mappings) {
        String base = srcCfg != null ? srcCfg.getBaseUrl() : System.getenv().getOrDefault("JIRA_BASE_URL", "");
        String email = srcCfg != null ? srcCfg.getUsername() : System.getenv().getOrDefault("JIRA_EMAIL", "");
        String token = srcCfg != null ? srcCfg.getApiToken() : System.getenv().getOrDefault("JIRA_API_TOKEN", "");
//        List<Event> specs = new JiraManger().fetch(base, email, token, task.getJqlExpression());
//        if (specs == null) specs = new ArrayList<>();
//        if (mappings == null || mappings.isEmpty()) return specs;
//        for (Event s : specs) FieldMapper.apply(s, mappings);
//        return specs;
        return new ArrayList<>();
    }
}
