package com.calsync.web.dto;

public class FieldMappingDTO {
    private String jiraField;
    private String radicateField;
    private String transformType;

    public String getJiraField() { return jiraField; }
    public void setJiraField(String jiraField) { this.jiraField = jiraField; }
    public String getRadicateField() { return radicateField; }
    public void setRadicateField(String radicateField) { this.radicateField = radicateField; }
    public String getTransformType() { return transformType; }
    public void setTransformType(String transformType) { this.transformType = transformType; }
}
