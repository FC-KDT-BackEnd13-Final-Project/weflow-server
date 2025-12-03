package com.rdc.weflow_server.dto.user.request;

import com.rdc.weflow_server.entity.company.Company;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.entity.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password; // 입력받은 그대로의 비밀번호 (암호화 전)

    private String phoneNumber;  // 선택사항

    @NotNull(message = "역할은 필수입니다")
    private UserRole role;  // SYSTEM_ADMIN, AGENCY, CLIENT

    @NotNull(message = "회사 ID는 필수입니다")
    private Long companyId;

    // DTO -> Entity 변환 메서드
    public User toEntity(Company company, String encodedPassword) {
        return User.builder()
                .name(this.name)
                .email(this.email)
                .password(encodedPassword)
                .phoneNumber(this.phoneNumber)
                .role(this.role)
                .company(company)
                .build();
    }
}