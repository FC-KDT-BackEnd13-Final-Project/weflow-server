package com.rdc.weflow_server.dto.project.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminProjectMemberAddResponse {
    private Long userId;
    private String projectRole;

    public static AdminProjectMemberAddResponse of(Long userId, String projectRole) {
        return AdminProjectMemberAddResponse.builder()
                .userId(userId)
                .projectRole(projectRole)
                .build();
    }
}
