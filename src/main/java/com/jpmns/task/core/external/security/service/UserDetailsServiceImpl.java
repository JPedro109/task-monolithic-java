package com.jpmns.task.core.external.security.service;

import java.util.Collections;

import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.jpmns.task.core.application.usecase.user.dto.input.GetUserByIdInputDTO;
import com.jpmns.task.core.application.usecase.user.interfaces.GetUserByIdUseCase;

@Component
@NullMarked
public class UserDetailsServiceImpl implements UserDetailsService {

    private final GetUserByIdUseCase getUserByIdUseCase;

    public UserDetailsServiceImpl(GetUserByIdUseCase getUserByIdUseCase) {
        this.getUserByIdUseCase = getUserByIdUseCase;
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        var input = new GetUserByIdInputDTO(userId);
        var model = getUserByIdUseCase.execute(input);

        return new User(model.id(), model.password(), Collections.emptyList());
    }
}
