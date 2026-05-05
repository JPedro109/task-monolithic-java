package com.jpmns.task.core.external.persistence.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jpmns.task.core.external.persistence.model.TaskJpaModel;

public interface TaskJpaDao extends JpaRepository<TaskJpaModel, UUID> {

    List<TaskJpaModel> findAllByUserId(UUID userId);
}
