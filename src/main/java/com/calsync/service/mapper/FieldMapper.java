package com.calsync.service.mapper;

import com.calsync.sync.Event;
import com.calsync.web.dto.FieldMappingDTO;
import java.time.Instant;
import java.util.List;

/**
 * 字段映射工具：根据映射规则将源字段赋值到目标事件规范字段。
 */
public final class FieldMapper {
    private FieldMapper() {
    }
    
    public static void apply(Event s, List<FieldMappingDTO> mappings) {
        if (s == null || mappings == null || mappings.isEmpty()) return;
        for (FieldMappingDTO m : mappings) {
            String src = m.getJiraField();
            String dst = m.getRadicateField();
            if (dst == null) continue;
            if ("summary".equalsIgnoreCase(dst)) s.setSummary(pickString(s, src));
            else if ("description".equalsIgnoreCase(dst)) s.setDescription(pickString(s, src));
            else if ("location".equalsIgnoreCase(dst)) s.setLocation(pickString(s, src));
            else if ("start".equalsIgnoreCase(dst)) s.setStart(pickInstant(s, src, s.getStart()));
            else if ("end".equalsIgnoreCase(dst)) s.setEnd(pickInstant(s, src, s.getEnd()));
            else if ("url".equalsIgnoreCase(dst)) s.setUrl(pickString(s, src));
            else if ("organizer".equalsIgnoreCase(dst)) s.setOrganizer(pickString(s, src));
            else if ("externalId".equalsIgnoreCase(dst)) s.setExternalId(pickString(s, src));
            else if ("rrule".equalsIgnoreCase(dst)) s.setRrule(pickString(s, src));
        }
    }
    
    private static String pickString(Event s, String src) {
        if (src == null) return null;
        switch (src.toLowerCase()) {
            case "summary":
                return s.getSummary();
            case "description":
                return s.getDescription();
            case "location":
                return s.getLocation();
            default:
                return s.getSummary();
        }
    }
    
    private static Instant pickInstant(Event s, String src, Instant fallback) {
        if (src == null) return fallback;
        switch (src.toLowerCase()) {
            case "start":
                return s.getStart();
            case "end":
                return s.getEnd();
            default:
                return fallback;
        }
    }
}
