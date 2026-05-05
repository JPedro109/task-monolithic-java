package com.jpmns.task.core.external.persistence.mapper;

import java.util.UUID;

import com.jpmns.task.core.domain.user.UserEntity;
import com.jpmns.task.core.external.persistence.model.UserJpaModel;

public class UserMapper {

    private UserMapper() { }

    public static UserJpaModel toModel(UserEntity entity) {
        return new UserJpaModel(
                UUID.fromString(entity.getId().asString()),
                entity.getUsername().asString(),
                entity.getPassword().asString(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static UserEntity toDomain(UserJpaModel model) {
        return new UserEntity(
                model.getId().toString(),
                model.getUsername(),
                model.getPassword(),
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
    }
}
