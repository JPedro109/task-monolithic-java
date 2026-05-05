package com.jpmns.task.core.external.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.jpmns.task.core.application.port.persistence.repository.UserRepository;
import com.jpmns.task.core.domain.common.valueobject.IdValueObject;
import com.jpmns.task.core.domain.user.UserEntity;
import com.jpmns.task.core.domain.user.valueobject.UsernameValueObject;
import com.jpmns.task.core.external.persistence.dao.UserJpaDao;
import com.jpmns.task.core.external.persistence.mapper.UserMapper;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaDao jpaRepository;

    public UserRepositoryAdapter(UserJpaDao jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UserEntity save(UserEntity user) {
        var model = UserMapper.toModel(user);

        var saved = jpaRepository.save(model);

        return UserMapper.toDomain(saved);
    }

    @Override
    public Optional<UserEntity> findById(IdValueObject id) {
        var parsedId = UUID.fromString(id.asString());

        return jpaRepository.findById(parsedId).map(UserMapper::toDomain);
    }

    @Override
    public Optional<UserEntity> findByUsername(UsernameValueObject username) {
        return jpaRepository.findByUsername(username.asString()).map(UserMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(UsernameValueObject username) {
        return jpaRepository.existsByUsername(username.asString());
    }

    @Override
    public void deleteById(IdValueObject id) {
        var parsedId = UUID.fromString(id.asString());

        jpaRepository.deleteById(parsedId);
    }
}
