package com.calsync.service.impl;

import com.calsync.domain.ServiceConfig;
import com.calsync.repository.ServiceConfigRepository;
import com.calsync.service.ServiceConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceConfigServiceImpl implements ServiceConfigService {
    @Autowired
    private ServiceConfigRepository repository;
    
    @Override
    public ServiceConfig getConfig(Long id) {
        return repository.findById(id).orElse(null);
    }
}
