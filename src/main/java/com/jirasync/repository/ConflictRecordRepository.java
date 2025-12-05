package com.jirasync.repository;

import com.jirasync.domain.ConflictRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConflictRecordRepository extends JpaRepository<ConflictRecord, Long> {
}
