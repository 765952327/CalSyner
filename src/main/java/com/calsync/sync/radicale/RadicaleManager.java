package com.calsync.sync.radicale;

import biweekly.ICalendar;
import com.calsync.domain.ServiceType;
import com.calsync.domain.SyncTask;
import com.calsync.service.SyncTaskService;
import com.calsync.sync.Event;
import com.calsync.sync.EventConverter;
import com.calsync.sync.EventSource;
import com.calsync.sync.EventTarget;
import com.calsync.sync.ParamsSource;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RadicaleManager extends RadicaleClientService implements EventSource, EventTarget, ParamsSource<RadicaleParam> {
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
    @Autowired
    private EventConverter<ICalendar> calendarConverter;
    @Override
    public List<Event> fetch(Long taskId) {
        SyncTask task = syncTaskService.getTask(taskId);
        if (task == null) {
            return new ArrayList<>();
        }
        Long radicaleId = task.getRadicaleConfigId();
        RadicaleClient client = getClient(radicaleId);
        List<ICalendar> iCalendars = client.queryAll();
        return calendarConverter.convert(iCalendars, null);
    }
    
    
    @Override
    public void push(Long taskId, List<Event> events) {
        SyncTask task = syncTaskService.getTask(taskId);
        if (task == null) {
            return;
        }
        List<ICalendar> iCalendars = calendarConverter.reverseConvert(events);
        RadicaleClient client = getClient(task.getRadicaleConfigId());
        
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
