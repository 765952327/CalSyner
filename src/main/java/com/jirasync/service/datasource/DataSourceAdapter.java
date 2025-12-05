package com.jirasync.service.datasource;

import com.jirasync.domain.ServiceConfig;
import com.jirasync.domain.SyncTask;
import com.jirasync.web.dto.FieldMappingDTO;
import com.winstone.sync.EventSpec;
import java.util.List;

public interface DataSourceAdapter {
    List<EventSpec> fetch(ServiceConfig srcCfg, SyncTask task, List<FieldMappingDTO> mappings);
}
