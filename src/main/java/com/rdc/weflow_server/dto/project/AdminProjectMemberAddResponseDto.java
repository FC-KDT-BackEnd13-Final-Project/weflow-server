package com.rdc.weflow_server.dto.project;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminProjectMemberAddResponseDto {
    private Long userId;
    private String projectRole;

    public static AdminProjectMemberAddResponseDto of(Long userId, String projectRole) {
        return AdminProjectMemberAddResponseDto.builder()
                .userId(userId)
                .projectRole(projectRole)
                .build();
    }
}
