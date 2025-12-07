package com.rdc.weflow_server.dto.project.request;

import com.rdc.weflow_server.entity.project.ProjectStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AdminProjectUpdateRequest {

    private String name;
    private String description;
    private ProjectStatus status;

    private LocalDateTime startDate;
    private LocalDateTime endDateExpected;
    private LocalDateTime endDate;

    private Long contractAmount;
    private String contractFileUrl;

    private Long customerCompanyId;
}
