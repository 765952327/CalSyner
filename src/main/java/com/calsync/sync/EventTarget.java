package com.calsync.sync;

import java.util.List;

public interface EventTarget {
    void push(Long taskId, List<Event> events);
}
