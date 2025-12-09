package com.rdc.weflow_server.dto.project.response;

import com.rdc.weflow_server.entity.project.ProjectMember;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProjectMemberResponse {

    private Long projectMemberId;      // project_member ID
    private Long userId;
    private String name;
    private String email;
    private String companyName;

    private String userRole;      // CLIENT / AGENCY
    private String projectRole;   // ADMIN / MEMBER

    private LocalDateTime joinedAt;
    private LocalDateTime removedAt;

    public static ProjectMemberResponse from(ProjectMember pm) {
        return ProjectMemberResponse.builder()
                .projectMemberId(pm.getId())
                .userId(pm.getUser().getId())
                .name(pm.getUser().getName())
                .email(pm.getUser().getEmail())
                .companyName(pm.getUser().getCompany().getName())
                .userRole(pm.getUser().getRole().name())
                .projectRole(pm.getRole().name())
                .joinedAt(pm.getCreatedAt())
                .removedAt(pm.getDeletedAt())
                .build();
    }
}
