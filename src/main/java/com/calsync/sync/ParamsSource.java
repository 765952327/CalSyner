package com.calsync.sync;

import java.util.List;

/**
 * 参数源
 */
public interface ParamsSource<T> {
    List<T> getParams(Long taskId);
}
