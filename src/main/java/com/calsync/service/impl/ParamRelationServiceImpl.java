package com.calsync.service.impl;

import com.calsync.domain.ParamRelation;
import com.calsync.repository.ParamRelationRepository;
import com.calsync.service.ParamRelationService;
import com.calsync.sync.Event;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 参数映射服务实现：根据任务的字段映射配置，将源对象属性按模板转换并填充到统一事件模型。
 * <p>
 * 功能说明：
 * - 从任务对应的 {@link ParamRelation} 中读取 JSON 配置（数组，每项包含 `source` 与 `target`）；
 * - `source` 为模板字符串，使用花括号占位（如 `{key}-{summary}`），占位符对应源对象的属性名；
 * - `target` 为目标事件的字段名（如 `summary`），将模板解析结果设置到该字段；
 * - 支持基础类型转换：当目标字段类型为 {@link Instant}、{@link Integer}、{@link Long} 时尝试格式化/解析。
 */
@Component
public class ParamRelationServiceImpl implements ParamRelationService {
    @Autowired
    private ParamRelationRepository repo;
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([^}]+)\\}");
    
    /**
     * 将任意源对象依照任务的字段映射规则转换为统一的 {@link Event}。
     * @param taskId 任务 ID，用于查询映射配置
     * @param source 源对象，模板占位符从该对象的属性中取值
     * @param <S>    源对象类型
     * @return 填充后的事件对象；当不存在配置或源为空时返回空事件对象
     */
    @Override
    public <S> Event toEvent(Long taskId, S source) {
        Event target = new Event();
        if (taskId == null || source == null) {
            return target;
        }
        
        ParamRelation relation = repo.getByTaskId(taskId);
        if (relation == null || relation.getRelation() == null || relation.getRelation().trim().isEmpty()) {
            return target;
        }
        
        String relationConfig = relation.getRelation();
        List<Map<String, String>> mappings = parseMappings(relationConfig);
        if (mappings.isEmpty()) {
            return target;
        }
        
        PropertyAccessor srcAccessor = PropertyAccessorFactory.forBeanPropertyAccess(source);
        PropertyAccessor dstAccessor = PropertyAccessorFactory.forBeanPropertyAccess(target);
        
        for (Map<String, String> m : mappings) {
            String srcTpl = m.getOrDefault("source", "");
            String tgtField = normalizeField(m.getOrDefault("target", ""));
            if (tgtField.isEmpty()) continue;
            
            String resolved = resolveTemplate(srcTpl, srcAccessor);
            Object value = convertForField(dstAccessor, tgtField, resolved);
            try {
                dstAccessor.setPropertyValue(tgtField, value);
            } catch (Exception ignored) {
            }
        }
        
        return target;
    }
    
    /**
     * 解析 JSON 映射配置为列表。
     * @param json 映射配置 JSON，形如：[{"source":"{key}-{summary}","target":"summary"}]
     * @return 映射项列表
     */
    private List<Map<String, String>> parseMappings(String json) {
        try {
            List<Map<String, String>> list = MAPPER.readValue(json, new TypeReference<List<Map<String, String>>>() {
            });
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * 依据占位模板从源对象读取属性并替换。
     * @param template 占位模板，例如："{key}-{summary}"
     * @param accessor 源对象属性访问器
     * @return 解析后的字符串；占位不存在或为 null 时替换为空串
     */
    private String resolveTemplate(String template, PropertyAccessor accessor) {
        if (template == null || template.isEmpty()) return "";
        Matcher m = PLACEHOLDER.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String prop = m.group(1).trim();
            Object val;
            try {
                val = accessor.getPropertyValue(prop);
            } catch (Exception e) {
                val = null;
            }
            String rep = val == null ? "" : String.valueOf(val);
            m.appendReplacement(sb, Matcher.quoteReplacement(rep));
        }
        m.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * 针对目标字段类型做必要的值转换。
     * @param dstAccessor 目标对象属性访问器
     * @param field       目标字段名
     * @param text        原始字符串值
     * @return 转换后的值对象
     */
    private Object convertForField(PropertyAccessor dstAccessor, String field, String text) {
        Class<?> type;
        try {
            type = dstAccessor.getPropertyType(field);
        } catch (Exception e) {
            type = String.class;
        }
        if (type == null) type = String.class;
        
        if (Instant.class.isAssignableFrom(type)) {
            return parseInstant(text);
        } else if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)) {
            try {
                return Integer.parseInt(text);
            } catch (Exception ignored) {
                return null;
            }
        } else if (Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type)) {
            try {
                return Long.parseLong(text);
            } catch (Exception ignored) {
                return null;
            }
        }
        return text;
    }
    
    /**
     * 将字符串解析为 {@link Instant}，支持 ISO-8601 与毫秒时间戳。
     * @param s 字符串值
     * @return 解析成功的 {@link Instant}，失败返回 {@code null}
     */
    private Instant parseInstant(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Instant.parse(s.trim());
        } catch (Exception ignored) {
        }
        try {
            return Instant.ofEpochMilli(Long.parseLong(s.trim()));
        } catch (Exception ignored) {
        }
        return null;
    }
    
    /**
     * 规范化字段名：去除包裹占位的花括号。
     * @param raw 原始字段名（可能形如 "{summary}")
     * @return 去除花括号后的字段名
     */
    private String normalizeField(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if (s.startsWith("{") && s.endsWith("}")) {
            return s.substring(1, s.length() - 1).trim();
        }
        return s;
    }
}
