package com.rdc.weflow_server.dto.company.request;

import com.rdc.weflow_server.entity.company.Company;
import com.rdc.weflow_server.entity.company.CompanyStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompanyRequest {

    @NotBlank(message = "회사명은 필수입니다")
    private String name;  // 회사명 (필수)

    @Pattern(regexp = "^[0-9-]*$", message = "사업자등록번호는 숫자와 하이픈(-)만 입력 가능합니다")
    private String businessNumber;  // 사업자등록번호 (선택)

    private String representative;  // 대표자명 (선택)

    @Email(message = "올바른 이메일 형식이어야 합니다")
    private String email;  // 대표 이메일 (선택) - 입력값이 있을 때만 형식 검사함

    private String address;  // 주소 (선택)

    private String memo;  // 메모 (선택)

    // DTO -> Entity 변환 메서드
    public Company toEntity() {
        return Company.builder()
                .name(this.name)
                .businessNumber(StringUtils.hasText(this.businessNumber) ? this.businessNumber : null)
                .representative(StringUtils.hasText(this.representative) ? this.representative : null)
                .email(StringUtils.hasText(this.email) ? this.email : null)
                .address(StringUtils.hasText(this.address) ? this.address : null)
                .memo(StringUtils.hasText(this.memo) ? this.memo : null)
                .status(CompanyStatus.ACTIVE)
                .build();
    }
}