package com.rdc.weflow_server.dto.project;

import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDetailResponseDto {

    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;

    private LocalDateTime startDate;
    private LocalDateTime endDateExpected;

    private Long contractAmount;

    public static ProjectDetailResponseDto from(Project project) {
        return ProjectDetailResponseDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
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
