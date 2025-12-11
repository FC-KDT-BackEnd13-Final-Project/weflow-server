package com.rdc.weflow_server.dto.user.request;

import com.rdc.weflow_server.entity.user.UserRole;
import com.rdc.weflow_server.entity.user.UserStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter  // @ModelAttribute로 쿼리 파라미터 받으려면 Setter 필요
public class UserSearchCondition {

    private String keyword;     // 검색어 (이름 또는 이메일)
    private UserRole role;      // 역할 필터 (SYSTEM_ADMIN, AGENCY, CLIENT)
    private UserStatus status;  // 상태 필터 (INVITED, ACTIVE, SUSPENDED, DELETED)
    private Long companyId;     // 회사 필터
}