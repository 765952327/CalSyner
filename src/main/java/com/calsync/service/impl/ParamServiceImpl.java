package com.calsync.service.impl;

import com.calsync.domain.ServiceType;
import com.calsync.repository.ParamRelationRepository;
import com.calsync.service.ParamService;
import com.calsync.sync.Param;
import com.calsync.sync.ParamsSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParamServiceImpl implements ParamService {
    private final static Map<ServiceType, ParamsSource<?>> paramsSourceMap = new HashMap<>();
    
    public ParamServiceImpl(List<ParamsSource<?>> paramsSources) {
        for (ParamsSource<?> paramsSource : paramsSources) {
            paramsSourceMap.put(paramsSource.getServiceType(), paramsSource);
        }
    }
    
    @Override
    public List<Param> getParams(Long taskId, ServiceType serviceType) {
        ParamsSource<? extends Param> source = paramsSourceMap.get(serviceType);
        if (source == null) {
            throw new RuntimeException("No params source found for service type " + serviceType);
        }
        List<? extends Param> params = source.getParams(taskId);
        return new ArrayList<>(params);
    }
}
