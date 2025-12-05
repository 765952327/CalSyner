package com.calsync.repository;

import com.calsync.domain.ConflictRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConflictRecordRepository extends JpaRepository<ConflictRecord, Long> {
}
