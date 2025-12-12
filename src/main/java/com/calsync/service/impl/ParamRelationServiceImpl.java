package com.calsync.service.impl;

import com.calsync.domain.ParamRelation;
import com.calsync.repository.ParamRelationRepository;
import com.calsync.service.ParamRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class ParamRelationServiceImpl implements ParamRelationService {
    @Autowired
    private ParamRelationRepository repo;
    
    @Override
    public String getConfig(Long taskId) {
        ParamRelation relation = repo.getByTaskId(taskId);
        if (relation == null) {
            return null;
        }
        return relation.getRelation();
    }
}
