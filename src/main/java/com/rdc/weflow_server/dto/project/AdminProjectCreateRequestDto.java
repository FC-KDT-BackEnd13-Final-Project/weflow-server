package com.rdc.weflow_server.dto.project;

import com.rdc.weflow_server.entity.company.Company;
import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import com.rdc.weflow_server.entity.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AdminProjectCreateRequestDto {

    private String name;
    private String description;
    private ProjectStatus status;

    private LocalDateTime startDate;
    private LocalDateTime endDateExpected;

    private Long contractAmount;
    private String contractFileUrl;

    private Long customerCompanyId;

    public Project toEntity(Company company, Long creatorId) {
        return Project.builder()
                .name(name)
                .description(description)
                .status(status)
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
