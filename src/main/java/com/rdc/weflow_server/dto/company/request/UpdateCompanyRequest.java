package com.rdc.weflow_server.dto.company.request;

import com.rdc.weflow_server.entity.company.CompanyStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateCompanyRequest {
    private String name;            // 회사명
    private String businessNumber;  // 사업자등록번호
    private String representative;  // 대표자명
    private String email;           // 이메일
    private String address;         // 주소
    private String memo;            // 메모
    private CompanyStatus status;   // 상태 (ACTIVE, INACTIVE)
}