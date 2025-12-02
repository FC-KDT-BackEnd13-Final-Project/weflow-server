package com.rdc.weflow_server.dto.project;

import com.rdc.weflow_server.entity.project.Project;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminProjectListResponseDto {
    private long totalCount;
    private int page;
    private int size;
    private List<AdminProjectSummaryDto> projects;

    public static AdminProjectListResponseDto of(
            List<Project> projects,
            long totalCount,
            int page,
            int size
    ) {
        return AdminProjectListResponseDto.builder()
                .totalCount(totalCount)
                .page(page)
                .size(size)
                .projects(
                        projects.stream()
                                .map(AdminProjectSummaryDto::from)
                                .toList()
                )
                .build();
    }
}
