package com.calsync.sync;

import com.calsync.domain.ServiceType;
import java.util.List;

/**
 * 参数源
 */
public interface ParamsSource<T extends Param> {
    List<T> getParams(Long taskId);
    
    ServiceType getServiceType();
}
