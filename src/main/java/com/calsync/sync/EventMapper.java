package com.calsync.sync;

import java.util.List;

public interface EventMapper {
    List<EventSpec> toEvents();
}
