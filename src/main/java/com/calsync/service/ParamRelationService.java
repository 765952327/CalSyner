package com.calsync.service;

import com.calsync.sync.Event;

public interface ParamRelationService {
    <S> Event toEvent(Long taskId, S source);
}
