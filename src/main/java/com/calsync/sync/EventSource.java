package com.calsync.sync;

import com.calsync.domain.ServiceConfig;
import com.calsync.domain.SyncTask;
import com.calsync.web.dto.FieldMappingDTO;
import java.util.List;

/**
 * 事件数据源接口：定义从源系统拉取事件规范数据的能力。
 */
public interface EventSource {
    /**
     * 拉取事件数据。
     * @param srcCfg 源服务配置
     * @param task 同步任务定义
     * @param mappings 字段映射配置
     * @return 事件规范列表
     */
    List<EventSpec> fetch(ServiceConfig srcCfg, SyncTask task, List<FieldMappingDTO> mappings);
}

