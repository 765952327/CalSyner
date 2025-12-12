package com.calsync.service.impl;

import com.calsync.domain.ParamRelation;
import com.calsync.repository.ParamRelationRepository;
import com.calsync.service.ParamRelationService;
import com.calsync.sync.Event;
import com.calsync.sync.EventType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 参数关系转换服务实现类。
 * <p>
 * 职责：根据任务的参数关系配置，将任意源对象（Map 或 POJO）转换为统一的事件模型对象。
 * 配置示例：[{"source":"{key}-{summary}","target":"summary"}]，其中：
 * - source 为模板字符串，花括号中的标识代表源对象字段名；
 * - target 为 Event 的目标字段名，将模板解析出的值赋到该字段。
 */
@Component
public class ParamRelationServiceImpl implements ParamRelationService {
    @Autowired
    private ParamRelationRepository repo;

    /**
     * 将源对象依据任务的字段关系配置转换为事件对象。
     *
     * @param taskId 任务 ID，用于加载对应的字段关系配置
     * @param source 源对象，支持 Map 或普通 POJO
     * @param <T> 返回类型（实际返回 Event，会进行泛型转换）
     * @param <S> 源对象类型
     * @return 事件对象；若配置缺失或解析失败，返回字段为空的 Event
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T, S> T toEvent(Long taskId, S source) {
        Event target = new Event();
        if (taskId == null) {
            return (T) target;
        }
        ParamRelation relation = repo.getByTaskId(taskId);
        if (relation == null) {
            return (T) target;
        }
        String relationConfig = relation.getRelation();
        if (relationConfig == null || relationConfig.trim().isEmpty()) {
            return (T) target;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<MapRule> rules = mapper.readValue(relationConfig, new TypeReference<List<MapRule>>() {});
            if (rules == null) {
                rules = new ArrayList<>();
            }
            for (MapRule r : rules) {
                if (r == null) continue;
                String tpl = r.getSource();
                String field = r.getTarget();
                if (field == null || field.trim().isEmpty()) continue;
                String value = applyTemplate(tpl, source);
                assign(target, field, value);
            }
        } catch (Exception ignored) {
        }
        return (T) target;
    }

    /**
     * 解析模板字符串，将形如 {field} 的占位符替换为源对象中的对应字段值。
     *
     * @param template 模板字符串
     * @param source   源对象
     * @param <S>      源对象类型
     * @return 替换完成后的字符串结果；若字段不存在则替换为空串
     */
    private <S> String applyTemplate(String template, S source) {
        if (template == null) return null;
        Pattern p = Pattern.compile("\\{([^}]+)\\}");
        Matcher m = p.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            Object val = readField(source, key);
            String rep = val != null ? String.valueOf(val) : "";
            m.appendReplacement(sb, Matcher.quoteReplacement(rep));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * 读取源对象中的指定字段值，支持 Map、Getter 方法及字段反射。
     *
     * @param src  源对象
     * @param name 字段名
     * @return 字段值；不存在或异常时返回 null
     */
    private Object readField(Object src, String name) {
        if (src == null || name == null) return null;
        try {
            if (src instanceof Map) {
                return ((Map<?, ?>) src).get(name);
            }
            String getter = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            Method m = null;
            try { m = src.getClass().getMethod(getter); } catch (NoSuchMethodException ignored) {}
            if (m != null) return m.invoke(src);
            Field f = null;
            try { f = src.getClass().getDeclaredField(name); } catch (NoSuchFieldException ignored) {}
            if (f != null) { f.setAccessible(true); return f.get(src); }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 将字符串值赋到 Event 指定字段，支持字符串/时间/数字与枚举类型。
     *
     * @param e     事件对象
     * @param field 目标字段名（不区分大小写）
     * @param value 要赋予的字符串值
     */
    private void assign(Event e, String field, String value) {
        if (e == null || field == null) return;
        switch (field.toLowerCase()) {
            case "uid":
                e.setUid(value);
                break;
            case "summary":
                e.setSummary(value);
                break;
            case "description":
                e.setDescription(value);
                break;
            case "location":
                e.setLocation(value);
                break;
            case "url":
                e.setUrl(value);
                break;
            case "organizer":
                e.setOrganizer(value);
                break;
            case "priority":
                try { e.setPriority(value != null ? Integer.valueOf(value) : null); } catch (Exception ignored) {}
                break;
            case "start":
                e.setStart(parseInstant(value));
                break;
            case "end":
                e.setEnd(parseInstant(value));
                break;
            case "createdat":
            case "created_at":
                e.setCreatedAt(parseInstant(value));
                break;
            case "updatedat":
            case "updated_at":
                e.setUpdatedAt(parseInstant(value));
                break;
            case "eventtype":
                if (value != null) {
                    if ("todo".equalsIgnoreCase(value)) e.setEventType(EventType.TODO);
                    else e.setEventType(EventType.EVENT);
                }
                break;
            case "key":
                e.setKey(value);
                break;
            default:
                // 未知字段名忽略
        }
    }

    /**
     * 解析时间字符串为 Instant，支持 ISO-8601 与毫秒时间戳。
     *
     * @param s 时间字符串
     * @return 解析得到的 Instant；失败返回 null
     */
    private Instant parseInstant(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Instant.parse(s); } catch (Exception ignored) {}
        try { return Instant.ofEpochMilli(Long.parseLong(s.trim())); } catch (Exception ignored) {}
        return null;
    }

    /**
     * 字段关系规则模型。
     * 包含模板来源（source）与目标字段（target）。
     */
    static class MapRule {
        private String source;
        private String target;
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }
    }
}
