package com.calsync.sync.jira;

import com.calsync.sync.Param;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JiraParam implements Param {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Schema {
        private String type;
        private String custom;
        private Integer customId;
    }
    
    private String id;
    private String name;
    private Boolean custom;
    private Boolean orderable;
    private Boolean navigable;
    private Boolean searchable;
    private String[] clauseNames;
    private Schema schema;
    
    @Override
    public String getKey() {
        return id;
    }
    
    @Override
    public String getAlias() {
        return name;
    }
}
