package com.rdc.weflow_server.repository.user;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rdc.weflow_server.dto.user.request.UserSearchCondition;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.entity.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.rdc.weflow_server.entity.user.QUser.user;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<User> searchUsers(UserSearchCondition condition, Pageable pageable) {
        // 1. 컨텐츠 조회 쿼리
        List<User> content = queryFactory
                .selectFrom(user)
                .leftJoin(user.company).fetchJoin() // 성능 최적화 (N+1 방지)
                .where(
                        keywordContains(condition.getKeyword()), // 검색어 조건
                        roleEq(condition.getRole()),             // 역할 조건
                        statusEq(condition.getStatus()),         // 상태 조건
                        companyIdEq(condition.getCompanyId()),   // 회사 조건
                        excludeSystemAdmin()                     // SYSTEM_ADMIN 제외
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(user.id.desc()) // 최신순 정렬
                .fetch();

        // 2. 카운트 쿼리 (페이징용)
        JPAQuery<Long> countQuery = queryFactory
                .select(user.count())
                .from(user)
                .where(
                        keywordContains(condition.getKeyword()),
                        roleEq(condition.getRole()),
                        statusEq(condition.getStatus()),
                        companyIdEq(condition.getCompanyId()),
                        excludeSystemAdmin()                     // SYSTEM_ADMIN 제외
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<String> findExistingEmails(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .select(user.email)
                .from(user)
                .where(user.email.in(emails))
                .fetch();
    }

    // --- 동적 쿼리 조건 메서드들 ---

    // 키워드 검색 (이름 OR 이메일 포함)
    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword)
                ? user.name.contains(keyword).or(user.email.contains(keyword))
                : null;
    }

    // 역할 필터
    private BooleanExpression roleEq(com.rdc.weflow_server.entity.user.UserRole role) {
        return role != null ? user.role.eq(role) : null;
    }

    // 상태 필터
    private BooleanExpression statusEq(com.rdc.weflow_server.entity.user.UserStatus status) {
        return status != null ? user.status.eq(status) : null;
    }

    // 회사 ID 필터
    private BooleanExpression companyIdEq(Long companyId) {
        return companyId != null ? user.company.id.eq(companyId) : null;
    }

    // SYSTEM_ADMIN 제외 (회원 관리 페이지용)
    private BooleanExpression excludeSystemAdmin() {
        return user.role.ne(UserRole.SYSTEM_ADMIN);
    }
}