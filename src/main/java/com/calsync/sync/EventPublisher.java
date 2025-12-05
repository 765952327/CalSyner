package com.calsync.sync;

import java.util.List;

/**
 * 事件发布接口：定义将事件发布到目标系统（如 CalDAV）的能力。
 */
public interface EventPublisher {
    /**
     * 发布或更新事件。
     * @param specs 事件规范列表
     */
    void upsert(List<EventSpec> specs);
}

