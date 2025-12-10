package com.calsync.service;

import com.calsync.domain.ServiceType;
import com.calsync.sync.Param;
import java.util.List;

public interface ParamService {
    List<Param> getParams(Long taskId, ServiceType serviceType);
}
