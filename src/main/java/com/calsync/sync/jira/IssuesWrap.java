package com.calsync.sync.jira;

import com.calsync.sync.Event;
import com.calsync.sync.EventMapper;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import net.rcarz.jiraclient.Issue;

public class IssuesWrap implements EventMapper, Iterator<Issue> {
    private final List<Issue> issues;
    private int cursor = 0;
    private int lastReturned = -1;
    
    
    public IssuesWrap(List<Issue> issues) {
        this.issues = issues;
    }
    
    @Override
    public List<Event> toEvents() {
        
        return null;
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
