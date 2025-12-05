package com.jirasync.repository;

import com.jirasync.domain.SyncDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncDetailRepository extends JpaRepository<SyncDetail, Long> {
}
