package com.jpmns.task.core.external.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jpmns.task.core.domain.common.valueobject.IdValueObject;
import com.jpmns.task.core.domain.user.UserEntity;
import com.jpmns.task.core.domain.user.valueobject.UsernameValueObject;
import com.jpmns.task.core.external.persistence.dao.UserJpaDao;
import com.jpmns.task.core.external.persistence.model.UserJpaModel;
import com.jpmns.task.shared.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock
    private UserJpaDao jpaRepository;

    @InjectMocks
    private UserRepositoryAdapter adapter;

    private UserJpaModel buildUserModel(UserEntity user) {
        var userId = user.getId();
        var username = user.getUsername();
        var password = user.getPassword();

        return new UserJpaModel(
                UUID.fromString(userId.asString()),
                username.asString(),
                password.asString(),
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    @DisplayName("Should save a user and return the persisted domain entity")
    void shouldSaveUser() {
        var user = UserFixture.aUser();
        var userId = user.getId();
        var username = user.getUsername();
        var model = buildUserModel(user);

        when(jpaRepository.save(any())).thenReturn(model);

        var result = adapter.save(user);

        assertThat(result).isNotNull();
        assertThat(result.getId().asString()).isEqualTo(userId.asString());
        assertThat(result.getUsername().asString()).isEqualTo(username.asString());
        verify(jpaRepository).save(any());
    }

    @Test
    @DisplayName("Should find a user by id and return the domain entity")
    void shouldFindUserById() {
        var user = UserFixture.aUser();
        var userId = user.getId();
        var id = IdValueObject.of(userId.asString()).getValue();
        var model = buildUserModel(user);

        when(jpaRepository.findById(UUID.fromString(userId.asString()))).thenReturn(Optional.of(model));

        var result = adapter.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId().asString()).isEqualTo(userId.asString());
    }

    @Test
    @DisplayName("Should return empty Optional when user is not found by id")
    void shouldReturnEmptyWhenUserNotFoundById() {
        var user = UserFixture.aUser();
        var userId = user.getId();
        var id = IdValueObject.of(userId.asString()).getValue();

        when(jpaRepository.findById(any())).thenReturn(Optional.empty());

        var result = adapter.findById(id);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find a user by username and return the domain entity")
    void shouldFindUserByUsername() {
        var user = UserFixture.aUser();
        var username = user.getUsername();
        var model = buildUserModel(user);

        when(jpaRepository.findByUsername(username.asString())).thenReturn(Optional.of(model));

        var result = adapter.findByUsername(username);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername().asString()).isEqualTo(username.asString());
    }

    @Test
    @DisplayName("Should return empty Optional when user is not found by username")
    void shouldReturnEmptyWhenUserNotFoundByUsername() {
        var username = UsernameValueObject.of("unknown").getValue();

        when(jpaRepository.findByUsername(any())).thenReturn(Optional.empty());

        var result = adapter.findByUsername(username);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return true when username already exists")
    void shouldReturnTrueWhenUsernameExists() {
        var user = UserFixture.aUser();
        var username = user.getUsername();

        when(jpaRepository.existsByUsername(username.asString())).thenReturn(true);

        var result = adapter.existsByUsername(username);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when username does not exist")
    void shouldReturnFalseWhenUsernameDoesNotExist() {
        var username = UsernameValueObject.of("nonexistent").getValue();

        when(jpaRepository.existsByUsername("nonexistent")).thenReturn(false);

        var result = adapter.existsByUsername(username);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should delete a user by id")
    void shouldDeleteUserById() {
        var user = UserFixture.aUser();
        var userId = user.getId();
        var id = IdValueObject.of(userId.asString()).getValue();

        doNothing().when(jpaRepository).deleteById(any());

        adapter.deleteById(id);

        verify(jpaRepository).deleteById(UUID.fromString(userId.asString()));
    }
}
