package com.winstone.caldav;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RadicalePublisher {
    private final Map<String, String> events = new ConcurrentHashMap<>();
    private final Map<String, String> todos = new ConcurrentHashMap<>();

    public List<String> listSummaries() {
        return new ArrayList<>(events.keySet());
    }

    public int deleteBySummary(String summary) {
        boolean existed = events.remove(summary) != null | todos.remove(summary) != null;
        return existed ? 200 : 404;
    }

    public int replaceBySummary(String ics, String uid, String summary) {
        events.put(summary, ics);
        return 200;
    }

    public int replaceTodoBySummary(String ics, String uid, String summary) {
        todos.put(summary, ics);
        return 200;
    }

    public int deleteEventsBySummary(String summary) {
        boolean existed = events.remove(summary) != null;
        return existed ? 200 : 404;
    }
}
