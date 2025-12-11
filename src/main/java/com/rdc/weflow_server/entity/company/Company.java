package com.rdc.weflow_server.entity.company;

import com.rdc.weflow_server.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.springframework.util.StringUtils;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "companies")
@Entity
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String businessNumber;

    @Column
    private String representative;

    @Column(unique = true)
    @Email
    private String email;

    @Column
    private String address;

    @Column
    private String memo;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CompanyStatus status;

    // 회사 정보 수정 메서드
    public void updateCompany(String name, String businessNumber, String representative, String email, String address, String memo, CompanyStatus status) {
        if (name != null) this.name = name;
        // 빈 문자열은 null로 변환 (unique 제약 조건 때문)
        if (businessNumber != null) this.businessNumber = StringUtils.hasText(businessNumber) ? businessNumber : null;
        if (representative != null) this.representative = StringUtils.hasText(representative) ? representative : null;
        if (email != null) this.email = StringUtils.hasText(email) ? email : null;
        if (address != null) this.address = StringUtils.hasText(address) ? address : null;
        if (memo != null) this.memo = StringUtils.hasText(memo) ? memo : null;
        if (status != null) this.status = status;
    }

    /**
     * [관리자] 회사 삭제 처리
     * - Soft Delete (삭제 시간 기록)
     * - 상태를 INACTIVE(비활성)로 변경
     */
    public void delete() {
        this.softDelete(); // BaseEntity의 메서드
        this.status = CompanyStatus.INACTIVE; // 상태 변경
    }

    /**
     * [관리자] 회사 복구
     * - 삭제 취소 (deletedAt = null)
     * - 상태를 ACTIVE로 변경
     */
    @Override
    public void restore() {
        super.restore(); // BaseEntity의 restore() 메서드 호출 (deletedAt = null)
        this.status = CompanyStatus.ACTIVE; // 상태 복구
    }
}