package com.calsync.sync;

import java.time.Instant;
import java.util.List;
import lombok.Data;

/**
 * 事件规范模型：描述可被发布到日历/待办的统一事件结构。
 */
@Data
public class EventSpec {
    public String summary;
    public Instant start;
    public Instant end;
    public String description;
    public String location;

    // 扩展字段
    public String uid;             // 目标系统唯一标识（可选）
    public boolean allDay;         // 是否为整天事件
    public String externalId;      // 源系统标识（如 Jira issue key）
    public String url;             // 源或目标详情链接
    public Integer priority;       // 优先级
    public List<String> categories;// 分类/标签
    public List<String> attendees; // 参与者（邮箱或名称）
    public String organizer;       // 组织者
    public String rrule;           // 重复规则（RRULE 原文）
    public Instant createdAt;      // 源创建时间
    public Instant updatedAt;      // 源更新时间

    public EventSpec() {}

    public EventSpec(String summary, Instant start, Instant end) {
        this.summary = summary;
        this.start = start;
        this.end = end;
    }
}
