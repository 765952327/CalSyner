package com.calsync.sync;

import java.time.Instant;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 事件规范模型：描述可被发布到日历/待办的统一事件结构。
 */
@Data
@NoArgsConstructor
public class EventSpec {
    private String summary;
    private Instant start;
    private Instant end;
    private String description;
    private String location;
    
    // 扩展字段
    private String uid;             // 目标系统唯一标识（可选）
    private boolean allDay;         // 是否为整天事件
    private String externalId;      // 源系统标识（如 JiraManger issue key）
    private String url;             // 源或目标详情链接
    private Integer priority;       // 优先级
    private List<String> categories;// 分类/标签
    private List<String> attendees; // 参与者（邮箱或名称）
    private String organizer;       // 组织者
    private String rrule;           // 重复规则（RRULE 原文）
    private Instant createdAt;      // 源创建时间
    private Instant updatedAt;      // 源更新时间
    
    
    public EventSpec(String summary, Instant start, Instant end) {
        this.summary = summary;
        this.start = start;
        this.end = end;
    }
}
