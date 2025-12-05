package com.calsync.repository;

import com.calsync.domain.SyncDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncDetailRepository extends JpaRepository<SyncDetail, Long> {
}
