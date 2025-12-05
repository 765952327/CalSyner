package com.jirasync.domain;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "conflict_records")
public class ConflictRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "task_id", nullable = false)
    private Long taskId;
    @Column(name = "item_id", length = 255, nullable = false)
    private String itemId;
    @Column(name = "item_type", length = 50, nullable = false)
    private String itemType;
    @Column(name = "source_data", columnDefinition = "TEXT", nullable = false)
    private String sourceData;
    @Column(name = "target_data", columnDefinition = "TEXT", nullable = false)
    private String targetData;
    @Column(name = "conflict_type", length = 100, nullable = false)
    private String conflictType;
    @Column(name = "resolution", length = 50)
    private String resolution;
    @Column(name = "created_at")
    private Instant createdAt;
    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getSourceData() { return sourceData; }
    public void setSourceData(String sourceData) { this.sourceData = sourceData; }
    public String getTargetData() { return targetData; }
    public void setTargetData(String targetData) { this.targetData = targetData; }
    public String getConflictType() { return conflictType; }
    public void setConflictType(String conflictType) { this.conflictType = conflictType; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
}
