package com.jpmns.task.core.application.port.persistence.repository;

import java.util.Optional;

import com.jpmns.task.core.domain.common.valueobject.IdValueObject;
import com.jpmns.task.core.domain.user.UserEntity;
import com.jpmns.task.core.domain.user.valueobject.UsernameValueObject;

public interface UserRepository {

    UserEntity save(UserEntity user);

    Optional<UserEntity> findById(IdValueObject id);

    Optional<UserEntity> findByUsername(UsernameValueObject username);

    boolean existsByUsername(UsernameValueObject username);

    void deleteById(IdValueObject id);
}
