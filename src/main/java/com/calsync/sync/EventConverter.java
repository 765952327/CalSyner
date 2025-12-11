package com.calsync.sync;

import java.util.List;

public interface EventConverter<T> {
    List<Event> convert(List<T> datas);
    
    List<T> reverseConvert(List<Event> events);
}
