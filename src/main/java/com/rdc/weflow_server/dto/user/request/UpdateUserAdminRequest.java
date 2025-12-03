package com.rdc.weflow_server.dto.user.request;

import com.rdc.weflow_server.entity.user.UserRole;
import com.rdc.weflow_server.entity.user.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserAdminRequest {
    private String name;
    private String phoneNumber;
    private UserRole role;      // 권한 변경 가능
    private UserStatus status;  // 상태 변경 가능 (ACTIVE, SUSPENDED 등)
    private Long companyId;     // 소속 회사 변경 가능
}