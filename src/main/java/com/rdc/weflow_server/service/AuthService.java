package com.rdc.weflow_server.service;

import com.rdc.weflow_server.common.auth.JwtTokenProvider;
import com.rdc.weflow_server.dto.request.LoginRequest;
import com.rdc.weflow_server.dto.response.LoginResponse;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.entity.user.UserStatus;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 1. 이메일로 사용자 찾기 (삭제된 사용자 제외)
        User user = userRepository.findByEmail(request.getEmail())
                .filter(u -> u.getDeletedAt() == null)  // Soft delete 체크
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 3. 계정 상태 확인
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.USER_SUSPENDED);
        }
        if (user.getStatus() == UserStatus.DELETED) {
            throw new BusinessException(ErrorCode.USER_DELETED);
        }

        // 4. 마지막 로그인 시간 업데이트
        user.updateLastLoginAt();

        // 5. JWT 토큰 생성
        String accessToken = jwtTokenProvider.createToken(
                user.getEmail(),
                user.getRole().name()
        );

        // 6. 응답 생성
        return LoginResponse.of(user, accessToken);
    }
}