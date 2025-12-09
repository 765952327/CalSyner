package com.calsync.sync.custom;

import com.calsync.domain.ServiceConfig;
import com.calsync.domain.SyncTask;
import com.calsync.sync.Event;
import com.calsync.web.dto.FieldMappingDTO;
import java.util.List;

/**
 * 自定义脚本接口：用户上传的 Java 代码需实现该接口以产出事件数据。
 */
public interface CustomScript {
    List<Event> run(ServiceConfig srcCfg, SyncTask task, List<FieldMappingDTO> mappings);
}
