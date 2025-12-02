package com.rdc.weflow_server.dto.project;

import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminProjectCreateResponseDto {

    private Long id;
    private String name;
    private ProjectStatus status;
    private LocalDateTime createdAt;

    public static AdminProjectCreateResponseDto from(Project project) {
        return AdminProjectCreateResponseDto.builder()
                .id(project.getId())
                .name(project.getName())
                .status(project.getStatus())
                .createdAt(project.getCreatedAt())
                .build();
    }
}
