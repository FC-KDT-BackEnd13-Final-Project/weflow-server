package com.rdc.weflow_server.dto.project.response;

import com.rdc.weflow_server.entity.project.ProjectMember;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectRoleUpdateResponse {

    private Long projectMemberId;
    private Long userId;
    private String name;
    private String newRole;

    public static ProjectRoleUpdateResponse from(ProjectMember pm) {
        return ProjectRoleUpdateResponse.builder()
                .projectMemberId(pm.getId())
                .userId(pm.getUser().getId())
                .name(pm.getUser().getName())
                .newRole(pm.getRole().name())
                .build();
    }
}
