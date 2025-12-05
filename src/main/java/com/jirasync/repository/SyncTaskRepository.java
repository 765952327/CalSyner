package com.jirasync.repository;

import com.jirasync.domain.SyncTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SyncTaskRepository extends JpaRepository<SyncTask, Long> {
    List<SyncTask> findByIsEnabledTrue();
}
