package com.rdc.weflow_server.dto.project.response;

import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.project.ProjectMember;
import com.rdc.weflow_server.entity.project.ProjectPhase;
import com.rdc.weflow_server.entity.project.ProjectRole;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectSummaryResponse {

    private Long projectId;
    private String name;
    private ProjectPhase phase;
    private ProjectStatus status;
    private String customerCompanyName;
    private LocalDateTime expirationDate;

    public static ProjectSummaryResponse from(Project p) {
        return ProjectSummaryResponse.builder()
                .projectId(p.getId())
                .name(p.getName())
                .phase(p.getPhase())
                .status(p.getStatus())
                .customerCompanyName(p.getCompany().getName())
                .expirationDate(p.getExpectedEndDate())
                .build();
    }
}
