package com.rdc.weflow_server.dto.project.response;

import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.project.ProjectMember;
import com.rdc.weflow_server.entity.project.ProjectRole;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectSummaryResponse {

    private Long projectId;
    private String name;
    private ProjectStatus status;
    private String customerCompanyName;

    public static ProjectSummaryResponse from(Project p) {
        return ProjectSummaryResponse.builder()
                .projectId(p.getId())
                .name(p.getName())
                .status(p.getStatus())
                .customerCompanyName(p.getCompany().getName())
                .build();
    }
}
