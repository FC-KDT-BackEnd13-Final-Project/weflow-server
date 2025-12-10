package com.rdc.weflow_server.dto.user.response;

import com.rdc.weflow_server.entity.user.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminUserResponse {

    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String role;
    private LocalDateTime deletedAt;

    public static AdminUserResponse from(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .deletedAt(user.getDeletedAt())
                .build();
    }
}
