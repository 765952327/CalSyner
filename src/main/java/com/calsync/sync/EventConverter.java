package com.calsync.sync;

import com.calsync.domain.ParamRelation;
import java.util.List;

public interface EventConverter<T> {
    List<Event> convert(List<T> datas, Long taskId);
    
    List<T> reverseConvert(List<Event> events);
}
