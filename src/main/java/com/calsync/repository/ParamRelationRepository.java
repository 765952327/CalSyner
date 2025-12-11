package com.calsync.repository;

import com.calsync.domain.ParamRelation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParamRelationRepository extends JpaRepository<ParamRelation,Long> {
    ParamRelation getByTaskId(Long taskId);
}
