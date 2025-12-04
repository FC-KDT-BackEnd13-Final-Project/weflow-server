package com.rdc.weflow_server.repository.company;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rdc.weflow_server.dto.company.request.CompanySearchCondition;
import com.rdc.weflow_server.entity.company.Company;
import com.rdc.weflow_server.entity.company.CompanyStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.rdc.weflow_server.entity.company.QCompany.company;

@RequiredArgsConstructor
public class CompanyRepositoryImpl implements CompanyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Company> searchCompanies(CompanySearchCondition condition, Pageable pageable) {
        // 1. 컨텐츠 조회 쿼리
        List<Company> content = queryFactory
                .selectFrom(company)
                .where(
                        keywordContains(condition.getKeyword()), // 검색어 조건
                        statusEq(condition.getStatus()),         // 상태 조건
                        company.deletedAt.isNull()               // 삭제되지 않은 회사만 조회
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(company.id.desc()) // 최신순 정렬
                .fetch();

        // 2. 카운트 쿼리 (페이징용)
        JPAQuery<Long> countQuery = queryFactory
                .select(company.count())
                .from(company)
                .where(
                        keywordContains(condition.getKeyword()),
                        statusEq(condition.getStatus()),
                        company.deletedAt.isNull()
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    // --- 동적 쿼리 조건 메서드들 ---

    // 키워드 검색 (회사명 OR 사업자번호 포함)
    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword)
                ? company.name.contains(keyword).or(company.businessNumber.contains(keyword))
                : null;
    }

    // 상태 필터
    private BooleanExpression statusEq(CompanyStatus status) {
        return status != null ? company.status.eq(status) : null;
    }
}