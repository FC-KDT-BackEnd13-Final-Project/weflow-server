package com.rdc.weflow_server.dto.project;

import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminProjectSummaryDto {
    private Long id;
    private String name;
    private ProjectStatus status;
    private Long customerCompanyId;
    private Long createdBy;

    private Boolean deleted;
    private LocalDateTime deletedAt;

    public static AdminProjectSummaryDto from(Project p) {
        return AdminProjectSummaryDto.builder()
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
