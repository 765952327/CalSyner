package com.calsync.sync;

import java.util.List;

/**
 * 事件数据源接口：定义从源系统拉取事件规范数据的能力。
 */
public interface EventSource {
    /**
     * 拉取事件数据。
     * @param taskId 同步任务
     * @return 事件规范列表
     */
    List<Event> fetch(Long taskId);
}

