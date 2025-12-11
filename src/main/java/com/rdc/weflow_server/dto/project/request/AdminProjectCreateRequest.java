package com.rdc.weflow_server.dto.project.request;

import com.rdc.weflow_server.dto.step.StepCreateRequest;
import com.rdc.weflow_server.entity.company.Company;
import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.project.ProjectPhase;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class AdminProjectCreateRequest {

    private String name;
    private String description;
    private ProjectPhase phase;

    private LocalDateTime startDate;
    private LocalDateTime endDateExpected;

    private Long contractAmount;
    private String contractFileUrl;

    private Long customerCompanyId;

    private List<StepCreateRequest> steps;

    public Project toEntity(Company company, Long creatorId) {
        return Project.builder()
                .name(name)
                .description(description)
                .phase(phase != null ? phase : ProjectPhase.CONTRACT) // null이면 기본값 CONTRACT
                .status(ProjectStatus.OPEN) // 생성 시 항상 OPEN
                .startDate(startDate)
                .endDate(null)
                .expectedEndDate(endDateExpected)
                .contractPrice(contractAmount != null ? BigDecimal.valueOf(contractAmount) : null)
                .contractFileUrl(contractFileUrl)
                .company(company)
                .createdBy(creatorId)
                .build();
    }
}
