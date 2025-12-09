package com.calsync.sync.jira;

import com.calsync.service.datasource.JiraDataSourceAdapter;
import com.calsync.sync.EventMapper;
import com.calsync.sync.EventSpec;
import com.calsync.sync.ParamsSource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import net.rcarz.jiraclient.Issue;

public class IssuesWrap implements EventMapper, ParamsSource, Iterator<Issue> {
    private final JiraDataSourceAdapter sourceAdapter = new JiraDataSourceAdapter();
    private final List<Issue> issues;
    private int cursor = 0;
    private int lastReturned = -1;
    
    
    public IssuesWrap(List<Issue> issues) {
        this.issues = issues;
    }
    
    @Override
    public List<EventSpec> toEvents() {
        
        return null;
    }
    
    
    @Override
    public List<JiraParam> getParams() {
        Issue issue = issues.get(0);
        if (issue == null) return java.util.Collections.emptyList();
        try {
            Field f = Issue.class.getDeclaredField("fields");
            f.setAccessible(true);
            Object obj = f.get(issue);
            if (obj instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) obj;
                List<JiraParam> ps = new ArrayList<>();
                for (Object k : map.keySet()) {
                    if (k != null) {
                        String name = String.valueOf(k);
                        ps.add(new JiraParam(name, name));
                    }
                }
                return ps;
            }
        } catch (Exception e) {
        }
        return java.util.Collections.emptyList();
    }
    
    @Override
    public boolean hasNext() {
        return issues != null && cursor < issues.size();
    }
    
    @Override
    public Issue next() {
        if (!hasNext()) throw new NoSuchElementException();
        lastReturned = cursor;
        return issues.get(cursor++);
    }
    
    @Override
    public void remove() {
        if (lastReturned < 0) throw new IllegalStateException();
        issues.remove(lastReturned);
        if (lastReturned < cursor) cursor--;
        lastReturned = -1;
    }
    
    @Override
    public void forEachRemaining(Consumer<? super Issue> action) {
        while (hasNext()) action.accept(next());
    }
}
