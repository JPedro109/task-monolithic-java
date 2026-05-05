package com.jpmns.task.core.external.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.jpmns.task.core.application.port.security.PasswordEncoder;

@Component
public class PasswordEncoderAdapter implements PasswordEncoder {

    private final BCryptPasswordEncoder bcrypt;

    public PasswordEncoderAdapter(BCryptPasswordEncoder bcrypt) {
        this.bcrypt = bcrypt;
    }

    @Override
    public String encode(String rawPassword) {
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return bcrypt.matches(rawPassword, encodedPassword);
    }
}
