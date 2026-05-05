package com.jpmns.task.core.external.persistence.mapper;

import java.util.UUID;

import com.jpmns.task.core.domain.task.TaskEntity;
import com.jpmns.task.core.external.persistence.model.TaskJpaModel;

public class TaskMapper {

    private TaskMapper() { }

    public static TaskJpaModel toModel(TaskEntity entity) {
        return new TaskJpaModel(
                UUID.fromString(entity.getId().asString()),
                UUID.fromString(entity.getUserId().asString()),
                entity.getTaskName().asString(),
                entity.getFinished(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static TaskEntity toDomain(TaskJpaModel model) {
        return new TaskEntity(
                model.getId().toString(),
                model.getUserId().toString(),
                model.getTaskName(),
                model.isFinished(),
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
    }
}
