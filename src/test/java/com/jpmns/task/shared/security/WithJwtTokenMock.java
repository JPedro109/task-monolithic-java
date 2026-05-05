package com.jpmns.task.shared.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithSecurityContext;

import com.jpmns.task.shared.security.factory.WithMockJwtTokenSecurityContextFactory;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockJwtTokenSecurityContextFactory.class)
public @interface WithJwtTokenMock {

    String sub() default "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

    HttpStatus httpStatus() default HttpStatus.OK;
}
