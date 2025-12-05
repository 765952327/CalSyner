package com.winstone.sync;

import java.time.Instant;

public class EventSpec {
    public String summary;
    public Instant start;
    public Instant end;
    public String description;
    public String location;

    public EventSpec() {}

    public EventSpec(String summary, Instant start, Instant end) {
        this.summary = summary;
        this.start = start;
        this.end = end;
    }
}
