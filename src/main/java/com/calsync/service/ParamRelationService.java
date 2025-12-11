package com.calsync.service;

import com.calsync.sync.Event;

public interface ParamRelationService {
    <T,S> T toEvent(Long taskId, S source);
}
