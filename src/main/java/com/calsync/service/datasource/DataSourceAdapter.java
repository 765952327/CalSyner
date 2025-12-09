package com.calsync.service.datasource;

import com.calsync.domain.ServiceConfig;
import com.calsync.domain.SyncTask;
import com.calsync.sync.Event;
import com.calsync.web.dto.FieldMappingDTO;
import java.util.List;

/**
 * 统一数据源适配器接口：用于从不同源系统（JiraManger、自定义脚本等）拉取事件数据。
 */
public interface DataSourceAdapter {
    /**
     * 拉取事件数据。
     * @param srcCfg 源服务配置
     * @param task 同步任务
     * @param mappings 字段映射配置
     * @return 事件规范列表
     */
    List<Event> fetch(ServiceConfig srcCfg, SyncTask task, List<FieldMappingDTO> mappings);
}
