package com.calsync.sync.jira;

import com.calsync.sync.Event;
import com.calsync.sync.ParamRelationHandler;
import java.util.Collections;
import java.util.List;
import net.rcarz.jiraclient.Issue;
import org.springframework.stereotype.Component;

@Component
public class JiraParamRelationHandler implements ParamRelationHandler<Event, Issue> {
    // config 记录的是 [{"source":"{key}{summary}","target":"{summary}"}] 格式的字符串。
    // 其中source记录的值是个模板，每个{}内都是 入参source中的字段，
    // 其中target记录的值是个字段名，需要将 source模板转换后的值设置到T的对应字段中
    // 以[{"source":"{key}-{summary}","target":"{summary}"}] 为例假设 source.key == "CLOUD",source.summary == "测试"
    // 那么target.summary 应该为 ”CLOUD-测试“
    
    @Override
    public List<Event> handle(List<Issue> sources, String config) {
        return Collections.emptyList();
    }
}
