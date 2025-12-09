package com.calsync.domain;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private Long radicaleConfigId;
    @Column(name = "jql_expression", columnDefinition = "TEXT", nullable = false)
    private String jqlExpression;
    @Column(name = "cron_expression", length = 100, nullable = false)
    private String cronExpression;
    @Column(name = "is_enabled")
    private Boolean isEnabled;
    @Column(name = "is_deleted")
    private Boolean isDeleted;
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
}
