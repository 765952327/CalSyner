package com.calsync.repository;

import com.calsync.domain.ServiceConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceConfigRepository extends JpaRepository<ServiceConfig, Long> {
}
