package com.rdc.weflow_server.config;

import com.rdc.weflow_server.common.auth.JwtAuthenticationFilter;
import com.rdc.weflow_server.common.auth.JwtTokenProvider;
import com.rdc.weflow_server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 세션 사용 안 함 (JWT 토큰 사용)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 경로별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 1. 누구나 접근 가능 (로그인, 에러)
                        .requestMatchers("/api/auth/**", "/error").permitAll()

                        // 2. 관리자만 접근 가능
                        .requestMatchers("/api/admin/**").hasRole("SYSTEM_ADMIN")

                        // 3. 나머지는 로그인 필요
                        .anyRequest().authenticated()
                )

                // JWT 필터 등록 (토큰 검증)
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, userRepository),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}