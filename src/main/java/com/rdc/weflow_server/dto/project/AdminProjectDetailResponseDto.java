package com.rdc.weflow_server.dto.project;

import com.rdc.weflow_server.entity.project.Project;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AdminProjectDetailResponseDto {

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

    private List<AdminProjectMemberDto> members;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminProjectDetailResponseDto from(Project p) {
        return AdminProjectDetailResponseDto.builder()
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
                                .map(m -> AdminProjectMemberDto.builder()
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
