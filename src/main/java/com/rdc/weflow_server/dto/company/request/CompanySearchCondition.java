package com.rdc.weflow_server.dto.company.request;

import com.rdc.weflow_server.entity.company.CompanyStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompanySearchCondition {
    private String keyword;       // 검색어 (회사명, 사업자번호 등)
    private CompanyStatus status; // 상태 필터 (ACTIVE, INACTIVE)
}