package com.calsync.sync.radicale;

import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.component.VTodo;
import biweekly.property.Created;
import biweekly.property.DateDue;
import biweekly.property.DateEnd;
import biweekly.property.DateStart;
import biweekly.property.Description;
import biweekly.property.LastModified;
import biweekly.property.Location;
import biweekly.property.Organizer;
import biweekly.property.Priority;
import biweekly.property.Summary;
import biweekly.property.Uid;
import biweekly.property.Url;
import cn.hutool.core.util.StrUtil;
import com.calsync.domain.ParamRelation;
import com.calsync.sync.Event;
import com.calsync.sync.EventConverter;
import com.calsync.sync.EventType;
import com.calsync.util.UIDUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * RadicaleEventConverter：在 ICalendar 与统一事件模型 Event 之间进行双向转换。
 * <p>
 * 主要功能：
 * - 将 CalDAV/CardDAV 解析得到的 {@link ICalendar} 列表转换为 {@link Event} 列表；
 * - 依据 {@link EventType} 将事件反向生成对应的 {@link VEvent}/{@link VTodo} 并封装为 {@link ICalendar}。
 */
@Component
public class RadicaleEventConverter implements EventConverter<ICalendar> {
    /**
     * ICalendar → Event 列表转换。
     *
     * @param datas ICalendar 列表（通常来源于 RadicaleClient.queryAll）
     * @return Event 列表；入参为空或无内容时返回空列表
     */
    @Override
    public List<Event> convert(List<ICalendar> datas, Long taskId) {
        if (datas == null || datas.isEmpty()) return Collections.emptyList();
        List<Event> events = new ArrayList<>();
        for (ICalendar cal : datas) {
            for (VEvent ev : cal.getEvents()) {
                Event e = new Event();
                Summary s = ev.getSummary();
                e.setSummary(s != null ? s.getValue() : null);
                Description d = ev.getDescription();
                e.setDescription(d != null ? d.getValue() : null);
                Location loc = ev.getLocation();
                e.setLocation(loc != null ? loc.getValue() : null);
                DateStart ds = ev.getDateStart();
                DateEnd de = ev.getDateEnd();
                if (ds != null && ds.getValue() != null) e.setStart(ds.getValue().toInstant());
                if (de != null && de.getValue() != null) e.setEnd(de.getValue().toInstant());
                Url urlProp = ev.getUrl();
                e.setUrl(urlProp != null && urlProp.getValue() != null ? urlProp.getValue().toString() : null);
                Organizer og = ev.getOrganizer();
                e.setOrganizer(og != null ? (og.getCommonName() != null ? og.getCommonName() : og.getEmail()) : null);
                Uid uid = ev.getUid();
                e.setUid(uid != null ? uid.getValue() : null);
                Priority pr = ev.getPriority();
                e.setPriority(pr != null ? pr.getValue() : null);
                Created cr = ev.getCreated();
                if (cr != null && cr.getValue() != null) e.setCreatedAt(cr.getValue().toInstant());
                LastModified lm = ev.getLastModified();
                if (lm != null && lm.getValue() != null) e.setUpdatedAt(lm.getValue().toInstant());
                e.setEventType(EventType.EVENT);
                events.add(e);
            }
            for (VTodo td : cal.getTodos()) {
                Event e = new Event();
                Summary s = td.getSummary();
                e.setSummary(s != null ? s.getValue() : null);
                Description d = td.getDescription();
                e.setDescription(d != null ? d.getValue() : null);
                Location loc = td.getLocation();
                e.setLocation(loc != null ? loc.getValue() : null);
                DateStart ds = td.getDateStart();
                DateDue due = td.getDateDue();
                if (ds != null && ds.getValue() != null) e.setStart(ds.getValue().toInstant());
                if (due != null && due.getValue() != null) e.setEnd(due.getValue().toInstant());
                Url urlProp = td.getUrl();
                e.setUrl(urlProp != null && urlProp.getValue() != null ? urlProp.getValue().toString() : null);
                Organizer og = td.getOrganizer();
                e.setOrganizer(og != null ? (og.getCommonName() != null ? og.getCommonName() : og.getEmail()) : null);
                Uid uid = td.getUid();
                e.setUid(uid != null ? uid.getValue() : null);
                Priority pr = td.getPriority();
                e.setPriority(pr != null ? pr.getValue() : null);
                Created cr = td.getCreated();
                if (cr != null && cr.getValue() != null) e.setCreatedAt(cr.getValue().toInstant());
                LastModified lm = td.getLastModified();
                if (lm != null && lm.getValue() != null) e.setUpdatedAt(lm.getValue().toInstant());
                e.setEventType(EventType.TODO);
                events.add(e);
            }
        }
        return events;
    }

    /**
     * Event → ICalendar 列表转换。
     * <p>
     * 简化策略：每个 Event 生成一个独立的 ICalendar，便于按 UID 进行单条写入与管理。
     *
     * @param events 事件列表
     * @return ICalendar 列表；入参为空返回空列表
     */
    @Override
    public List<ICalendar> reverseConvert(List<Event> events) {
        if (events == null || events.isEmpty()) return Collections.emptyList();
        List<ICalendar> out = new ArrayList<>();
        for (Event e : events) {
            if (StrUtil.isBlank(e.getUid())) {
                e.setUid(UIDUtils.toUid(e.getSummary()));
            }
            ICalendar cal = new ICalendar();
            if (e.getEventType() == EventType.TODO) {
                VTodo td = new VTodo();
                if (e.getUid() != null) td.setUid(new Uid(e.getUid() + "-TODO"));
                if (e.getSummary() != null) td.setSummary(new Summary(e.getSummary()));
                if (e.getDescription() != null) td.setDescription(new Description(e.getDescription()));
                if (e.getLocation() != null) td.setLocation(new Location(e.getLocation()));
                if (e.getStart() != null) td.setDateStart(new DateStart(Date.from(e.getStart())));
                if (e.getEnd() != null) td.setDateDue(new DateDue(Date.from(e.getEnd())));
                if (e.getUrl() != null) td.setUrl(new Url(e.getUrl()));
                if (e.getPriority() != null) td.setPriority(new Priority(e.getPriority()));
                if (e.getCreatedAt() != null) td.setCreated(new Created(Date.from(e.getCreatedAt())));
                if (e.getUpdatedAt() != null) td.setLastModified(new LastModified(Date.from(e.getUpdatedAt())));
                cal.addTodo(td);
            } else {
                VEvent ev = new VEvent();
                if (e.getUid() != null) ev.setUid(new Uid(e.getUid()));
                if (e.getSummary() != null) ev.setSummary(new Summary(e.getSummary()));
                if (e.getDescription() != null) ev.setDescription(new Description(e.getDescription()));
                if (e.getLocation() != null) ev.setLocation(new Location(e.getLocation()));
                if (e.getStart() != null) ev.setDateStart(new DateStart(Date.from(e.getStart())));
                if (e.getEnd() != null) ev.setDateEnd(new DateEnd(Date.from(e.getEnd())));
                if (e.getUrl() != null) ev.setUrl(new Url(e.getUrl()));
                if (e.getPriority() != null) ev.setPriority(new Priority(e.getPriority()));
                if (e.getCreatedAt() != null) ev.setCreated(new Created(Date.from(e.getCreatedAt())));
                if (e.getUpdatedAt() != null) ev.setLastModified(new LastModified(Date.from(e.getUpdatedAt())));
                cal.addEvent(ev);
            }
            out.add(cal);
        }
        return out;
    }
}
