package com.jirasync.domain;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "operation_logs")
public class OperationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "op_type", length = 50)
    private String opType;
    @Column(name = "summary", length = 255)
    private String summary;
    @Column(name = "target_type", length = 20)
    private String targetType;
    @Column(name = "radicate_uid", length = 100)
    private String radicateUid;
    @Column(name = "status", length = 20)
    private String status;
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    @Column(name = "record_id")
    private Long recordId;
    @Column(name = "task_id")
    private Long taskId;
    @Column(name = "created_at")
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOpType() { return opType; }
    public void setOpType(String opType) { this.opType = opType; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getRadicateUid() { return radicateUid; }
    public void setRadicateUid(String radicateUid) { this.radicateUid = radicateUid; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
