package com.rdc.weflow_server.dto.project.response;

import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.project.ProjectPhase;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminProjectSummary {
    private Long id;
    private String name;
    private ProjectPhase phase;
    private ProjectStatus status;
    private Long customerCompanyId;
    private String customerCompanyName;
    private Long createdBy;
    private String createdByName;

    private Boolean deleted;
    private LocalDateTime deletedAt;

    public static AdminProjectSummary from(Project p) {
        return AdminProjectSummary.builder()
                .id(p.getId())
                .name(p.getName())
                .phase(p.getPhase())
                .status(p.getStatus())
                .customerCompanyId(p.getCompany().getId())
                .customerCompanyName(p.getCompany().getName())
                .createdBy(p.getCreatedBy())
                .createdByName(p.getCreatedUser().getName())
                .deleted(p.getDeletedAt() != null)
                .deletedAt(p.getDeletedAt())
                .build();
    }
}
