package com.calsync.service.impl;

import com.calsync.sync.Event;
import com.calsync.sync.EventPublisher;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RadicateClientService implements EventPublisher {
    
    @Override
    public void upsert(List<Event> specs) {
    
    }
}
