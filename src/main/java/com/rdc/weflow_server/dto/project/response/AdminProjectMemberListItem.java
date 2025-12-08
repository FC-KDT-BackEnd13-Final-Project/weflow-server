package com.rdc.weflow_server.dto.project.response;

import com.rdc.weflow_server.entity.project.ProjectMember;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminProjectMemberListItem {
    private Long projectMemberId;
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String companyName;
    private String projectRole;
    private String userRole;
    private LocalDateTime createdAt;
    private LocalDateTime removedAt;

    public static AdminProjectMemberListItem from(ProjectMember pm) {
        return AdminProjectMemberListItem.builder()
                .projectMemberId(pm.getId())
                .userId(pm.getUser().getId())
                .username(pm.getUser().getName())
                .email(pm.getUser().getEmail())
                .phone(pm.getUser().getPhoneNumber())
                .companyName(pm.getUser().getCompany().getName())
                .projectRole(pm.getRole().name())
                .userRole(pm.getUser().getRole().name())
                .createdAt(pm.getCreatedAt())
                .removedAt(pm.getDeletedAt())
                .build();
    }
}
