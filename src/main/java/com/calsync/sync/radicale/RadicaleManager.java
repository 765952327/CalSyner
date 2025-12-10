package com.calsync.sync.radicale;

import biweekly.ICalendar;
import com.calsync.domain.ServiceType;
import com.calsync.domain.SyncTask;
import com.calsync.service.SyncTaskService;
import com.calsync.sync.Event;
import com.calsync.sync.EventSource;
import com.calsync.sync.EventTarget;
import com.calsync.sync.ParamsSource;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RadicaleManager implements EventSource, EventTarget, ParamsSource<RadicaleParam> {
    private static final List<RadicaleParam> params = new ArrayList<>();
    
    static {
        params.add(new RadicaleParam("summary", "摘要"));
        params.add(new RadicaleParam("start", "开始时间"));
        params.add(new RadicaleParam("end", "结束时间"));
        params.add(new RadicaleParam("description", "备注"));
        params.add(new RadicaleParam("url", "url"));
    }
    @Autowired
    private SyncTaskService syncTaskService;
    @Override
    public List<Event> fetch(Long taskId) {
        SyncTask task = syncTaskService.getTask(taskId);
        Long radicaleId = task.getRadicaleConfigId();
        return new ArrayList<>();
    }
    
    @Override
    public void push(List<Event> events) {
    
    }
    
    @Override
    public List<RadicaleParam> getParams(Long taskId) {
        return params;
    }
    
    @Override
    public ServiceType getServiceType() {
        return ServiceType.RADICALE;
    }
}
