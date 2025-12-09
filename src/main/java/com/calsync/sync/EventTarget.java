package com.calsync.sync;

import java.util.List;

public interface EventTarget {
    void push(List<Event> events);
}
