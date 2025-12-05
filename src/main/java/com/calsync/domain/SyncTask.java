package com.calsync.domain;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sync_tasks")
public class SyncTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "task_name", nullable = false, length = 100)
    private String taskName;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(name = "jira_config_id", nullable = false)
    private Long jiraConfigId;
    @Column(name = "radicate_config_id", nullable = false)
    private Long radicateConfigId;
    @Column(name = "jql_expression", columnDefinition = "TEXT", nullable = false)
    private String jqlExpression;
    @Column(name = "cron_expression", length = 100, nullable = false)
    private String cronExpression;
    @Column(name = "is_enabled")
    private Boolean isEnabled;
    @Column(name = "sync_status", length = 20)
    private String syncStatus;
    @Column(name = "last_sync_time")
    private Instant lastSyncTime;
    @Column(name = "next_sync_time")
    private Instant nextSyncTime;
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "created_at")
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getJiraConfigId() { return jiraConfigId; }
    public void setJiraConfigId(Long jiraConfigId) { this.jiraConfigId = jiraConfigId; }
    public Long getRadicateConfigId() { return radicateConfigId; }
    public void setRadicateConfigId(Long radicateConfigId) { this.radicateConfigId = radicateConfigId; }
    public String getJqlExpression() { return jqlExpression; }
    public void setJqlExpression(String jqlExpression) { this.jqlExpression = jqlExpression; }
    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
    public Boolean getIsEnabled() { return isEnabled; }
    public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }
    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
    public Instant getLastSyncTime() { return lastSyncTime; }
    public void setLastSyncTime(Instant lastSyncTime) { this.lastSyncTime = lastSyncTime; }
    public Instant getNextSyncTime() { return nextSyncTime; }
    public void setNextSyncTime(Instant nextSyncTime) { this.nextSyncTime = nextSyncTime; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
