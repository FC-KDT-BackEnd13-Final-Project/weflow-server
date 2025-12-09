package com.rdc.weflow_server.controller.company;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.company.request.CompanySearchCondition;
import com.rdc.weflow_server.dto.company.request.CreateCompanyRequest;
import com.rdc.weflow_server.dto.company.request.UpdateCompanyRequest;
import com.rdc.weflow_server.dto.company.response.CompanyResponse;
import com.rdc.weflow_server.service.company.CompanyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @Valid @RequestBody CreateCompanyRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest) {

        CompanyResponse response = companyService.createCompany(
                request,
                user.getId(),
                servletRequest.getRemoteAddr()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회사가 성공적으로 생성되었습니다.", response));
    }

    /**
     * 회사 목록 조회 (관리자 전용)
     * GET /api/admin/companies
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CompanyResponse>>> getCompanies(
            @ModelAttribute CompanySearchCondition condition,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<CompanyResponse> response = companyService.getCompanies(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success("회사 목록 조회 성공", response));
    }

    /**
     * 회사 상세 조회 (관리자 전용)
     * GET /api/admin/companies/{companyId}
     */
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyResponse>> getCompany(@PathVariable Long companyId) {
        CompanyResponse response = companyService.getCompany(companyId);
        return ResponseEntity.ok(ApiResponse.success("회사 상세 조회 성공", response));
    }

    /**
     * 회사 정보 수정 (관리자 전용)
     * PATCH /api/admin/companies/{companyId}
     */
    @PatchMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyResponse>> updateCompany(
            @PathVariable Long companyId,
            @RequestBody @Valid UpdateCompanyRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest
    ) {
        CompanyResponse response = companyService.updateCompany(
                companyId,
                request,
                user.getId(),
                servletRequest.getRemoteAddr()
        );
        return ResponseEntity.ok(ApiResponse.success("회사 정보 수정 성공", response));
    }

    /**
     * 회사 삭제 (관리자 전용)
     * DELETE /api/admin/companies/{companyId}
     */
    @DeleteMapping("/{companyId}")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(
            @PathVariable Long companyId,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest
    ) {
        companyService.deleteCompany(
                companyId,
                user.getId(),
                servletRequest.getRemoteAddr()
        );
        return ResponseEntity.ok(ApiResponse.success("회사 삭제 성공", null));
    }
}