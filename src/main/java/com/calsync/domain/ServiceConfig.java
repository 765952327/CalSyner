package com.calsync.domain;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "service_configs")
public class ServiceConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "service_type", nullable = false, length = 20)
    private String serviceType;
    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;
    @Column(name = "base_url", nullable = false, length = 500)
    private String baseUrl;
    @Column(length = 100)
    private String username;
    @Column(length = 255)
    private String password;
    @Column(name = "api_token", length = 500)
    private String apiToken;
    @Column(name = "connection_status", length = 20)
    private String connectionStatus;
    @Column(name = "last_test_time")
    private Instant lastTestTime;
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "created_at")
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getApiToken() { return apiToken; }
    public void setApiToken(String apiToken) { this.apiToken = apiToken; }
    public String getConnectionStatus() { return connectionStatus; }
    public void setConnectionStatus(String connectionStatus) { this.connectionStatus = connectionStatus; }
    public Instant getLastTestTime() { return lastTestTime; }
    public void setLastTestTime(Instant lastTestTime) { this.lastTestTime = lastTestTime; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
