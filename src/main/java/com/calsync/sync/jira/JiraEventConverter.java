package com.calsync.sync.jira;

import com.calsync.sync.Event;
import com.calsync.sync.EventConverter;
import java.util.Collections;
import java.util.List;
import net.rcarz.jiraclient.Issue;
import org.springframework.stereotype.Component;

@Component
public class JiraEventConverter implements EventConverter<Issue> {
    
    @Override
    public List<Event> convert(List<Issue> datas) {
        return Collections.emptyList();
    }
    
    @Override
    public List<Issue> reverseConvert(List<Event> events) {
        return Collections.emptyList();
    }
}
