package com.rdc.weflow_server.dto.project.response;

import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminProjectSummary {
    private Long id;
    private String name;
    private ProjectStatus status;
    private Long customerCompanyId;
    private Long createdBy;

    private Boolean deleted;
    private LocalDateTime deletedAt;

    public static AdminProjectSummary from(Project p) {
        return AdminProjectSummary.builder()
                .id(p.getId())
                .name(p.getName())
                .status(p.getStatus())
                .customerCompanyId(p.getCompany().getId())
                .createdBy(p.getCreatedBy())
                .deleted(p.getDeletedAt() != null)
                .deletedAt(p.getDeletedAt())
                .build();
    }
}
