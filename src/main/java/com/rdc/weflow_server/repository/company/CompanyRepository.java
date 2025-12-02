package com.rdc.weflow_server.repository.company;

import com.rdc.weflow_server.entity.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    // 사업자 번호 중복 체크
    boolean existsByBusinessNumber(String businessNumber);

    // 이메일 중복 체크
    boolean existsByEmail(String email);
}