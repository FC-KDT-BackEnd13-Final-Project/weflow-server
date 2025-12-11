package com.rdc.weflow_server.repository.company;

import com.rdc.weflow_server.entity.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CompanyRepository extends JpaRepository<Company, Long>, CompanyRepositoryCustom {

    // 사업자 번호 중복 체크
    boolean existsByBusinessNumber(String businessNumber);

    // 이메일 중복 체크
    boolean existsByEmail(String email);

    // 삭제되지 않은 회사 수
    @Query("SELECT COUNT(c) FROM Company c WHERE c.deletedAt IS NULL")
    long countActiveCompanies();
}