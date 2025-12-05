package com.calsync.repository;

import com.calsync.domain.SyncRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncRecordRepository extends JpaRepository<SyncRecord, Long> {
}
