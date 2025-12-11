package com.calsync.service.impl;

import com.calsync.domain.ParamRelation;
import com.calsync.repository.ParamRelationRepository;
import com.calsync.service.ParamRelationService;
import com.calsync.sync.Event;
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
    public <T, S> T toEvent(Long taskId, S source) {
        T target = null;
        ParamRelation relation = repo.getByTaskId(taskId);
        String relationConfig = relation.getRelation();
        // relationConfig 记录的是 [{"source":"{key}{summary}","target":"{summary}"}] 格式的字符串。
        // 其中source记录的值是个模板，每个{}内都是 入参source中的字段，
        // 其中target记录的值是个字段名，需要将 source模板转换后的值设置到T的对应字段中
        // 以[{"source":"{key}-{summary}","target":"{summary}"}] 为例假设 source.key == "CLOUD",source.summary == "测试"
        // 那么target.summary 应该为 ”CLOUD-测试“
        return target;
    }
}
