package com.rdc.weflow_server.dto.response;

import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.entity.user.UserRole;
import com.rdc.weflow_server.entity.user.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    // 기본 정보
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private UserRole role;
    private UserStatus status;
    private Boolean isTemporaryPassword;
    private LocalDateTime lastLoginAt;

    // 회사 정보 (Flattening)
    private Long companyId;
    private String companyName;

    // 메타 정보 (관리자 히스토리용)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity → DTO 변환 메서드
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .isTemporaryPassword(user.getIsTemporaryPassword())
                .lastLoginAt(user.getLastLoginAt())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .companyName(user.getCompany() != null ? user.getCompany().getName() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}