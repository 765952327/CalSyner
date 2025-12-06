package com.calsync.service.mapper;

import com.calsync.sync.EventSpec;
import com.calsync.web.dto.FieldMappingDTO;
import java.time.Instant;
import java.util.List;

/**
 * 字段映射工具：根据映射规则将源字段赋值到目标事件规范字段。
 */
public final class FieldMapper {
    private FieldMapper() {}

    public static void apply(EventSpec s, List<FieldMappingDTO> mappings) {
        if (s == null || mappings == null || mappings.isEmpty()) return;
        for (FieldMappingDTO m : mappings) {
            String src = m.getJiraField();
            String dst = m.getRadicateField();
            if (dst == null) continue;
            if ("summary".equalsIgnoreCase(dst)) s.summary = pickString(s, src);
            else if ("description".equalsIgnoreCase(dst)) s.description = pickString(s, src);
            else if ("location".equalsIgnoreCase(dst)) s.location = pickString(s, src);
            else if ("start".equalsIgnoreCase(dst)) s.start = pickInstant(s, src, s.start);
            else if ("end".equalsIgnoreCase(dst)) s.end = pickInstant(s, src, s.end);
            else if ("url".equalsIgnoreCase(dst)) s.url = pickString(s, src);
            else if ("organizer".equalsIgnoreCase(dst)) s.organizer = pickString(s, src);
            else if ("externalId".equalsIgnoreCase(dst)) s.externalId = pickString(s, src);
            else if ("rrule".equalsIgnoreCase(dst)) s.rrule = pickString(s, src);
        }
    }

    private static String pickString(EventSpec s, String src) {
        if (src == null) return null;
        switch (src.toLowerCase()) {
            case "summary": return s.summary;
            case "description": return s.description;
            case "location": return s.location;
            default: return s.summary;
        }
    }

    private static Instant pickInstant(EventSpec s, String src, Instant fallback) {
        if (src == null) return fallback;
        switch (src.toLowerCase()) {
            case "start": return s.start;
            case "end": return s.end;
            default: return fallback;
        }
    }
}
