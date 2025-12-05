package com.jirasync.repository;

import com.jirasync.domain.SyncRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncRecordRepository extends JpaRepository<SyncRecord, Long> {
}
