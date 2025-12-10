package com.calsync.service;

import com.calsync.domain.SyncTask;
import com.calsync.repository.SyncTaskRepository;
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
