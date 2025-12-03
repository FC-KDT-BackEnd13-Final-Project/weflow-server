package com.rdc.weflow_server.service.company;

import com.rdc.weflow_server.dto.company.request.CreateCompanyRequest;
import com.rdc.weflow_server.dto.company.response.CompanyResponse;
import com.rdc.weflow_server.entity.company.Company;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.company.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;

    /**
     * 관리자 - 회사 생성
     * POST /api/admin/companies
     */
    @Transactional
    public CompanyResponse createCompany(CreateCompanyRequest request) {
        // 1. 사업자번호 중복 체크 (입력값이 있을 때만)
        if (request.getBusinessNumber() != null &&
                companyRepository.existsByBusinessNumber(request.getBusinessNumber())) {
            throw new BusinessException(ErrorCode.COMPANY_BUSINESS_NUMBER_DUPLICATE);
        }

        // 2. 이메일 중복 체크 (입력값이 있을 때만)
        if (request.getEmail() != null &&
                companyRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.COMPANY_EMAIL_DUPLICATE);
        }

        // 3. DTO -> Entity 변환 후 저장
        Company company = request.toEntity();
        Company savedCompany = companyRepository.save(company);

        // 4. Entity -> Response DTO 변환하여 반환
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
}