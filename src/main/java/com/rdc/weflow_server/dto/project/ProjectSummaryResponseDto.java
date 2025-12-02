package com.rdc.weflow_server.dto.project;

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
public class ProjectSummaryResponseDto {

    private Long projectId;
    private String name;
    private ProjectStatus status;
    private ProjectRole projectRole;

    public static ProjectSummaryResponseDto from(ProjectMember pm) {
        Project p = pm.getProject();

        return ProjectSummaryResponseDto.builder()
                .projectId(p.getId())
                .name(p.getName())
                .status(p.getStatus())
                .projectRole(pm.getRole())
                .build();
    }
}
