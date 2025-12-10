package com.calsync.service.impl;

import com.calsync.domain.SyncTask;
import com.calsync.repository.SyncTaskRepository;
import com.calsync.service.SyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SyncTaskServiceImpl implements SyncTaskService {
    @Autowired
    private SyncTaskRepository repository;
    
    @Override
    public SyncTask getTask(Long id) {
        return repository.findById(id).orElse(null);
    }
}
