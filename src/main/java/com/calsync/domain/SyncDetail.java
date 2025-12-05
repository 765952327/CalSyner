package com.calsync.domain;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sync_details")
public class SyncDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "record_id", nullable = false)
    private Long recordId;
    @Column(name = "item_id", length = 100, nullable = false)
    private String itemId;
    @Column(name = "item_summary", length = 255)
    private String itemSummary;
    @Column(name = "action", length = 20, nullable = false)
    private String action;
    @Column(name = "status", length = 20, nullable = false)
    private String status;
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    @Column(name = "target_type", length = 20)
    private String targetType;
    @Column(name = "radicate_uid", length = 100)
    private String radicateUid;
    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;
    @Column(name = "created_at")
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getItemSummary() { return itemSummary; }
    public void setItemSummary(String itemSummary) { this.itemSummary = itemSummary; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getRadicateUid() { return radicateUid; }
    public void setRadicateUid(String radicateUid) { this.radicateUid = radicateUid; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
