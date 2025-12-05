package com.jirasync.service;

public class RadicateSyncResult {
    private String summary;
    private String uid;
    private int code;
    private String payload;
    private String targetType;

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
}
