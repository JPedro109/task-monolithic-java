package com.jpmns.task.core.external.security.service;

import java.util.Collections;

import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.jpmns.task.core.application.port.persistence.repository.UserRepository;
import com.jpmns.task.core.domain.common.valueobject.IdValueObject;

@Component
@NullMarked
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        var idResult = IdValueObject.of(userId);

        if (idResult.isFail()) {
            throw new UsernameNotFoundException("User not found: " + userId);
        }

        var model = userRepository.findById(idResult.getValue())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        return new User(model.getId().asString(), model.getPassword().asString(), Collections.emptyList());
    }
}
