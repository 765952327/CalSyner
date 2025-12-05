package com.winstone.custom;

import com.jirasync.domain.ServiceConfig;
import com.jirasync.domain.SyncTask;
import com.jirasync.web.dto.FieldMappingDTO;
import com.winstone.sync.EventSpec;
import java.util.List;

public interface CustomScript {
    List<EventSpec> run(ServiceConfig srcCfg, SyncTask task, List<FieldMappingDTO> mappings);
}
