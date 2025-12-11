package com.rdc.weflow_server.service.company;

import com.rdc.weflow_server.dto.company.request.CompanySearchCondition;
import com.rdc.weflow_server.dto.company.request.CreateCompanyRequest;
import com.rdc.weflow_server.dto.company.request.UpdateCompanyRequest;
import com.rdc.weflow_server.dto.company.response.CompanyResponse;
import com.rdc.weflow_server.entity.company.Company;
import com.rdc.weflow_server.entity.log.ActionType;
import com.rdc.weflow_server.entity.log.TargetTable;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.company.CompanyRepository;
import com.rdc.weflow_server.service.log.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final ActivityLogService activityLogService;

    /**
     * 관리자 - 회사 생성
     * POST /api/admin/companies
     */
    @Transactional
    public CompanyResponse createCompany(CreateCompanyRequest request, Long adminId, String ipAddress) {
        // 1. 사업자번호 중복 체크 (입력값이 있을 때만)
        if (StringUtils.hasText(request.getBusinessNumber()) &&
                companyRepository.existsByBusinessNumber(request.getBusinessNumber())) {
            throw new BusinessException(ErrorCode.COMPANY_BUSINESS_NUMBER_DUPLICATE);
        }

        // 2. 이메일 중복 체크 (입력값이 있을 때만)
        if (StringUtils.hasText(request.getEmail()) &&
                companyRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.COMPANY_EMAIL_DUPLICATE);
        }

        // 3. DTO -> Entity 변환 후 저장
        Company company = request.toEntity();
        Company savedCompany = companyRepository.save(company);

        // 4. 로그 기록
        activityLogService.createLog(
                ActionType.CREATE,
                TargetTable.COMPANY,
                savedCompany.getId(),
                adminId,
                null,
                ipAddress
        );

        // 5. Entity -> Response DTO 변환하여 반환
        return CompanyResponse.from(savedCompany);
    }

    /**
     * 내 회사 정보 조회
     * GET /api/companies/me
     */
    public CompanyResponse getMyCompany(Long companyId) {
        // 소속 회사가 없는 경우
        if (companyId == null) {
            throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        return CompanyResponse.from(company);
    }

    /**
     * 관리자 - 회사 목록 조회 (검색 + 페이징)
     * GET /api/admin/companies
     */
    public Page<CompanyResponse> getCompanies(CompanySearchCondition condition, Pageable pageable) {
        // 1. Repository에서 검색 (QueryDSL)
        Page<Company> companies = companyRepository.searchCompanies(condition, pageable);

        // 2. Entity -> DTO 변환
        return companies.map(CompanyResponse::from);
    }

    /**
     * 관리자 - 회사 상세 조회
     * GET /api/admin/companies/{companyId}
     */
    public CompanyResponse getCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        return CompanyResponse.from(company);
    }

    /**
     * 관리자 - 회사 정보 수정
     * PATCH /api/admin/companies/{companyId}
     */
    @Transactional
    public CompanyResponse updateCompany(Long companyId, UpdateCompanyRequest request, Long adminId, String ipAddress) {
        // 1. 대상 회사 조회
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        // 2. 이미 삭제된 회사인지 체크
        if (company.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.COMPANY_ALREADY_DELETED);
        }

        // 3. 엔티티의 수정 메서드 호출
        company.updateCompany(
                request.getName(),
                request.getBusinessNumber(),
                request.getRepresentative(),
                request.getEmail(),
                request.getAddress(),
                request.getMemo(),
                request.getStatus()
        );

        // 4. 로그 기록
        activityLogService.createLog(
                ActionType.UPDATE,
                TargetTable.COMPANY,
                company.getId(),
                adminId,
                null,
                ipAddress
        );

        // 5. 변경된 정보 반환
        return CompanyResponse.from(company);
    }

    /**
     * 관리자 - 회사 삭제 (Soft Delete)
     * DELETE /api/admin/companies/{companyId}
     */
    @Transactional
    public void deleteCompany(Long companyId, Long adminId, String ipAddress) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        // 이미 삭제된 회사인지 체크
        if (company.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.COMPANY_ALREADY_DELETED);
        }

        // Soft Delete 수행
        company.delete();

        // 로그 기록
        activityLogService.createLog(
                ActionType.DELETE,
                TargetTable.COMPANY,
                company.getId(),
                adminId,
                null,
                ipAddress
        );
    }

    /**
     * 관리자 - 회사 복구
     * PATCH /api/admin/companies/{companyId}/restore
     */
    @Transactional
    public CompanyResponse restoreCompany(Long companyId, Long adminId, String ipAddress) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        // 삭제되지 않은 회사는 복구 불가
        if (company.getDeletedAt() == null) {
            throw new BusinessException(ErrorCode.COMPANY_NOT_DELETED);
        }

        // 복구 수행
        company.restore();

        // 로그 기록
        activityLogService.createLog(
                ActionType.UPDATE,
                TargetTable.COMPANY,
                company.getId(),
                adminId,
                null,
                ipAddress
        );

        return CompanyResponse.from(company);
    }
}