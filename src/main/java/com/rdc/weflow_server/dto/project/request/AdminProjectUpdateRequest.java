package com.rdc.weflow_server.dto.project.request;

import com.rdc.weflow_server.dto.step.StepCreateRequest;
import com.rdc.weflow_server.entity.project.ProjectPhase;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class AdminProjectUpdateRequest {

    private String name;
    private String description;
    private ProjectPhase phase;
    private ProjectStatus status;

    private LocalDateTime startDate;
    private LocalDateTime endDateExpected;
    private LocalDateTime endDate;

    private Long contractAmount;
    private String contractFileUrl;

    private Long customerCompanyId;

    private List<StepCreateRequest> steps;
}
