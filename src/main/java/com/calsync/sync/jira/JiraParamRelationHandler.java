package com.calsync.sync.jira;

import com.calsync.sync.Event;
import com.calsync.sync.ParamRelationHandler;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.rcarz.jiraclient.Issue;
import org.springframework.stereotype.Component;

@Component
public class JiraParamRelationHandler implements ParamRelationHandler<Event, Issue> {
    // config 记录的是 [{"source":"{key}{summary}","target":"{summary}"}] 格式的字符串。
    // 其中source记录的值是个模板，每个{}内都是 入参source中的字段，
    // 其中target记录的值是个字段名，需要将 source模板转换后的值设置到T的对应字段中
    // 以[{"source":"{key}-{summary}","target":"{summary}"}] 为例假设 source.key == "CLOUD",source.summary == "测试"
    // 那么target.summary 应该为 ”CLOUD-测试“
    
    @Override
    public List<Event> handle(List<Issue> sources, String config) {
        if (sources == null || sources.isEmpty()) {
            return new ArrayList<>();
        }
        List<MappingRule> rules = parseRules(config);
        if (rules.isEmpty()) {
            rules = Collections.singletonList(new MappingRule("{summary}", "summary"));
        }
        List<Event> result = new ArrayList<>(sources.size());
        for (Issue issue : sources) {
            Event ev = new Event();
            try {
                Field fk = Event.class.getDeclaredField("key");
                fk.setAccessible(true);
                fk.set(ev, issue.getKey());
            } catch (Exception ignored) {}
            for (MappingRule r : rules) {
                String value = applyTemplate(r.source, issue);
                applyToEvent(ev, r.target, value);
            }
            result.add(ev);
        }
        return result;
    }
    
    private static class MappingRule {
        public String source;
        public String target;
        public MappingRule() {}
        public MappingRule(String source, String target) {
            this.source = source;
            this.target = target;
        }
    }
    
    private List<MappingRule> parseRules(String config) {
        if (config == null || config.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return JSONUtil.toBean(config, new TypeReference<List<MappingRule>>() {}, true);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    private String applyTemplate(String template, Issue issue) {
        if (template == null) return "";
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < template.length()) {
            char c = template.charAt(i);
            if (c == '{') {
                int j = template.indexOf('}', i + 1);
                if (j != -1) {
                    String field = template.substring(i + 1, j);
                    String v = readIssueField(issue, field);
                    sb.append(v != null ? v : "");
                    i = j + 1;
                    continue;
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }
    
    private String readIssueField(Issue issue, String field) {
        if (issue == null || field == null) return null;
        Object v = resolveIssueField(issue, field);
        return stringify(v);
    }
    
    private Object resolveIssueField(Issue issue, String fieldPath) {
        String[] parts = fieldPath.split("\\.");
        Object cur = readBase(issue, parts[0]);
        for (int i = 1; i < parts.length && cur != null; i++) {
            cur = dig(cur, parts[i]);
        }
        return cur;
    }
    
    private Object readBase(Issue issue, String name) {
        if (name == null) return null;
        switch (name) {
            case "key":
                return issue.getKey();
            case "summary":
                return issue.getSummary();
            case "description":
                return issue.getDescription();
            default:
                try {
                    return issue.getField(name);
                } catch (Exception e) {
                    return null;
                }
        }
    }
    
    private Object dig(Object obj, String name) {
        if (obj == null || name == null) return null;
        if (obj instanceof java.util.Map) {
            return ((java.util.Map<?, ?>) obj).get(name);
        }
        try {
            java.lang.reflect.Method m = obj.getClass().getMethod("get", Object.class);
            return m.invoke(obj, name);
        } catch (Exception ignored) {
        }
        return null;
    }
    
    private String stringify(Object v) {
        if (v == null) return null;
        String cn = v.getClass().getName();
        if ("net.sf.json.JSONNull".equals(cn)) return null;
        if (v instanceof java.util.Date) {
            return java.time.Instant.ofEpochMilli(((java.util.Date) v).getTime()).toString();
        }
        if (v instanceof java.util.Map) {
            Object nv = ((java.util.Map<?, ?>) v).get("name");
            if (nv == null) nv = ((java.util.Map<?, ?>) v).get("value");
            if (nv == null) nv = ((java.util.Map<?, ?>) v).get("id");
            return nv != null ? Objects.toString(nv, null) : JSONUtil.toJsonStr(v);
        }
        if (v instanceof java.util.List) {
            List<?> lst = (java.util.List<?>) v;
            List<String> ss = new ArrayList<>();
            for (Object o : lst) {
                ss.add(stringify(o));
            }
            return String.join(",", ss);
        }
        return Objects.toString(v, null);
    }
    
    private String safeString(Object o) {
        return o == null ? null : Objects.toString(o, null);
    }
    
    private void applyToEvent(Event ev, String target, String value) {
        if (ev == null || target == null) return;
        try {
            Field f = Event.class.getDeclaredField(target);
            f.setAccessible(true);
            Class<?> t = f.getType();
            Object v = convertValue(value, t);
            if (v != null) {
                f.set(ev, v);
            }
        } catch (Exception ignored) {
        }
    }
    
    private Object convertValue(String value, Class<?> t) {
        if (t == String.class) {
            return value;
        }
        if (t == Integer.class || t == int.class) {
            try {
                return value == null ? null : Integer.parseInt(value);
            } catch (Exception ignored) {
                return null;
            }
        }
        if (t == Instant.class) {
            try {
                return value == null ? null : Instant.parse(value);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }
}
