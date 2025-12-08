package com.rdc.weflow_server.dto.project.response;

import com.rdc.weflow_server.entity.project.Project;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AdminProjectDetailResponse {

    private Long id;
    private String name;
    private String description;
    private String status;

    private LocalDateTime startDate;
    private LocalDateTime endDateExpected;
    private LocalDateTime endDate;

    private Long contractAmount;
    private String contractFileUrl;

    private Long customerCompanyId;
    private Long createdBy;

    private Boolean deleted;
    private LocalDateTime deletedAt;

    private List<AdminProjectMember> members;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminProjectDetailResponse from(Project p) {
        return AdminProjectDetailResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .status(p.getStatus().name())

                .startDate(p.getStartDate())
                .endDateExpected(p.getExpectedEndDate())
                .endDate(p.getEndDate())

                .contractAmount(
                        p.getContractPrice() != null ? p.getContractPrice().longValue() : null
                )
                .contractFileUrl(p.getContractFileUrl())

                .customerCompanyId(p.getCompany().getId())
                .createdBy(p.getCreatedBy())

                .deleted(p.getDeletedAt() != null)
                .deletedAt(p.getDeletedAt())

                .members(
                        p.getProjectMembers()
                                .stream()
                                .map(m -> AdminProjectMember.builder()
                                        .userId(m.getUser().getId())
                                        .role(m.getRole().name())
                                        .build()
                                )
                                .toList()
                )

                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
