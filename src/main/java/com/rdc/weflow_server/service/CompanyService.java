package com.rdc.weflow_server.service;

import com.rdc.weflow_server.dto.request.CreateCompanyRequest;
import com.rdc.weflow_server.dto.response.CompanyResponse;
import com.rdc.weflow_server.entity.company.Company;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.company.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    /**
     * 회사 생성
     * - 사업자번호 중복 체크
     * - 이메일 중복 체크
     * - 회사 저장 후 Response DTO 반환
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
}