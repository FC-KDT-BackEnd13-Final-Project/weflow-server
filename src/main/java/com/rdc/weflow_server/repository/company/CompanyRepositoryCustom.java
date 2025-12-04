package com.rdc.weflow_server.repository.company;

import com.rdc.weflow_server.dto.company.request.CompanySearchCondition;
import com.rdc.weflow_server.entity.company.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyRepositoryCustom {
    Page<Company> searchCompanies(CompanySearchCondition condition, Pageable pageable);
}