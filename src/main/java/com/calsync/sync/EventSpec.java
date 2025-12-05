package com.calsync.sync;

import java.time.Instant;
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

    public EventSpec() {}

    public EventSpec(String summary, Instant start, Instant end) {
        this.summary = summary;
        this.start = start;
        this.end = end;
    }
}
