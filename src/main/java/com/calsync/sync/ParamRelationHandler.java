package com.calsync.sync;

import java.util.List;

public interface ParamRelationHandler<T, S> {
    List<T> handle(List<S> sources, String config);
}
