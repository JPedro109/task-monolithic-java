package com.jpmns.task.core.application.port.persistence.repository;

import java.util.List;
import java.util.Optional;

import com.jpmns.task.core.domain.common.valueobject.IdValueObject;
import com.jpmns.task.core.domain.task.TaskEntity;

public interface TaskRepository {

    TaskEntity save(TaskEntity task);

    Optional<TaskEntity> findById(IdValueObject id);

    List<TaskEntity> findAllByUserId(IdValueObject userId);

    void deleteById(IdValueObject id);
}
