package com.calsync.sync.jira;

import cn.hutool.core.collection.CollectionUtil;
import com.calsync.domain.ParamRelation;
import com.calsync.service.ParamRelationService;
import com.calsync.sync.Event;
import com.calsync.sync.EventConverter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.rcarz.jiraclient.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JiraEventConverter implements EventConverter<Issue> {
    @Autowired
    private ParamRelationService relationService;
    @Autowired
    private JiraParamRelationHandler paramRelationHandler;
    @Override
    public List<Event> convert(List<Issue> datas, Long taskId) {
        if (CollectionUtil.isEmpty(datas)) {
            return new ArrayList<>();
        }
        String config = relationService.getConfig(taskId);
        return paramRelationHandler.handle(datas, config);
    }
    
    @Override
    public List<Issue> reverseConvert(List<Event> events) {
        // 暂不处理
        return Collections.emptyList();
    }
}
