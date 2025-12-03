package com.rdc.weflow_server.controller.company;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.dto.company.request.CreateCompanyRequest;
import com.rdc.weflow_server.dto.company.response.CompanyResponse;
import com.rdc.weflow_server.service.company.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/companies")
@RequiredArgsConstructor
public class AdminCompanyController {

    private final CompanyService companyService;

    /**
     * 회사 생성 (관리자 전용)
     * POST /api/admin/companies
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(
            @Valid @RequestBody CreateCompanyRequest request) {

        CompanyResponse response = companyService.createCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회사가 성공적으로 생성되었습니다.", response));
    }
}