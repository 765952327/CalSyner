package com.jirasync.repository;

import com.jirasync.domain.ServiceConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceConfigRepository extends JpaRepository<ServiceConfig, Long> {
}
