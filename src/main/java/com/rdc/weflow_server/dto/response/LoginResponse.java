package com.rdc.weflow_server.dto.response;

import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.entity.user.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private UserInfo user;

    /**
     * 로그인한 사용자 간단 정보 (Inner Class)
     */
    @Getter
    @Builder
    public static class UserInfo {
        private Long id;
        private String email;
        private String name;
        private UserRole role;
    }

    /**
     * User 엔티티 + Token -> LoginResponse 변환
     */
    public static LoginResponse of(User user, String accessToken) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .user(UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .role(user.getRole())
                        .build())
                .build();
    }
}