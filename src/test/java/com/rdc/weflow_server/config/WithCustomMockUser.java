package com.rdc.weflow_server.config;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCustomMockUserSecurityContextFactory.class)
public @interface WithCustomMockUser {
    long id() default 1L;
    String email() default "admin@bn-system.com";
    String name() default "시스템관리자";
    String role() default "SYSTEM_ADMIN";
}
