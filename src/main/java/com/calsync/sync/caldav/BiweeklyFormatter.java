package com.calsync.sync.caldav;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.component.VTodo;
import com.calsync.sync.EventSpec;
import java.util.Date;

public class BiweeklyFormatter {
    public String format(EventSpec spec, String uid) {
        ICalendar cal = new ICalendar();
        VEvent ev = new VEvent();
        if (uid != null) ev.setUid(uid);
        if (spec.summary != null) ev.setSummary(spec.summary);
        if (spec.description != null) ev.setDescription(spec.description);
        if (!spec.allDay) {
            if (spec.start != null) ev.setDateStart(Date.from(spec.start));
            if (spec.end != null) ev.setDateEnd(Date.from(spec.end));
        } else {
            if (spec.start != null) ev.setDateStart(Date.from(spec.start), false);
            if (spec.end != null) ev.setDateEnd(Date.from(spec.end), false);
        }
        if (spec.location != null) ev.setLocation(spec.location);
        cal.addEvent(ev);
        return Biweekly.write(cal).go();
    }

    public String formatTodo(EventSpec spec, String uid) {
        ICalendar cal = new ICalendar();
        VTodo td = new VTodo();
        if (uid != null) td.setUid(uid);
        if (spec.summary != null) td.setSummary(spec.summary);
        if (spec.description != null) td.setDescription(spec.description);
        cal.addTodo(td);
        return Biweekly.write(cal).go();
    }
}
