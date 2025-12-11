package com.rdc.weflow_server.dto.project.response;

import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.project.ProjectPhase;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDetailResponse {

    private Long id;
    private String name;
    private String description;
    private ProjectPhase phase;
    private ProjectStatus status;

    private LocalDateTime startDate;
    private LocalDateTime endDateExpected;

    private Long contractAmount;

    public static ProjectDetailResponse from(Project project) {
        return ProjectDetailResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .phase(project.getPhase())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .endDateExpected(project.getExpectedEndDate())
                .contractAmount(
                        project.getContractPrice() != null
                                ? project.getContractPrice().longValue()
                                : null
                )
                .build();
    }
}
