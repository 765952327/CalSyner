package com.calsync.sync.caldav;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.component.VTodo;
import com.calsync.sync.Event;
import java.util.Date;

public class BiweeklyFormatter {
    public String format(Event spec, String uid) {
        ICalendar cal = new ICalendar();
        VEvent ev = new VEvent();
        if (uid != null) ev.setUid(uid);
        if (spec.getSummary() != null) ev.setSummary(spec.getSummary());
        if (spec.getDescription() != null) ev.setDescription(spec.getDescription());
        if (!spec.isAllDay()) {
            if (spec.getStart() != null) ev.setDateStart(Date.from(spec.getStart()));
            if (spec.getEnd() != null) ev.setDateEnd(Date.from(spec.getEnd()));
        } else {
            if (spec.getStart() != null) ev.setDateStart(Date.from(spec.getStart()), false);
            if (spec.getEnd() != null) ev.setDateEnd(Date.from(spec.getEnd()), false);
        }
        if (spec.getLocation() != null) ev.setLocation(spec.getLocation());
        cal.addEvent(ev);
        return Biweekly.write(cal).go();
    }

    public String formatTodo(Event spec, String uid) {
        ICalendar cal = new ICalendar();
        VTodo td = new VTodo();
        if (uid != null) td.setUid(uid);
        if (spec.getSummary() != null) td.setSummary(spec.getSummary());
        if (spec.getDescription() != null) td.setDescription(spec.getDescription());
        cal.addTodo(td);
        return Biweekly.write(cal).go();
    }
}
