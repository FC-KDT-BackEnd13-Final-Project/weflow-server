package com.rdc.weflow_server.dto.project.response;

import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminProjectCreateResponse {

    private Long id;
    private String name;
    private ProjectStatus status;
    private LocalDateTime createdAt;

    public static AdminProjectCreateResponse from(Project project) {
        return AdminProjectCreateResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .status(project.getStatus())
                .createdAt(project.getCreatedAt())
                .build();
    }
}
