package com.rdc.weflow_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())        // CSRF 비활성화
                .formLogin(login -> login.disable())  // 기본 로그인 폼 제거
                .httpBasic(basic -> basic.disable())  // 기본 인증 제거
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()     // 모든 요청 허용 (개발 단계)
                );

        return http.build();
    }
}

