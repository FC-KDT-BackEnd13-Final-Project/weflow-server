package com.rdc.weflow_server.dto.response;

import com.rdc.weflow_server.entity.company.Company;
import com.rdc.weflow_server.entity.company.CompanyStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CompanyResponse {

    private Long id;
    private String name;
    private String businessNumber;
    private String representative;
    private String email;
    private String address;
    private String memo;
    private CompanyStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity -> DTO 변환
    public static CompanyResponse from(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .businessNumber(company.getBusinessNumber())
                .representative(company.getRepresentative())
                .email(company.getEmail())
                .address(company.getAddress())
                .memo(company.getMemo())
                .status(company.getStatus())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}
