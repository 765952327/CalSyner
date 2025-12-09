package com.calsync.domain;

import javax.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "service_configs")
public class ServiceConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 20)
    private ServiceType serviceType;
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
}
