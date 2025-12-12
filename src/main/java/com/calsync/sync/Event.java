package com.calsync.sync;

import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 事件规范模型：描述可被发布到日历/待办的统一事件结构。
 */
@Data
@NoArgsConstructor
public class Event {
    private String uid;
    private String summary;
    private Instant start;
    private Instant end;
    private String description;
    private String location;
    private EventType eventType;
    
    // 扩展字段
    private String key;
    private String url;             // 源或目标详情链接
    private Integer priority;       // 优先级
    private String organizer;       // 组织者
    private Instant createdAt;      // 源创建时间
    private Instant updatedAt;      // 源更新时间
}
