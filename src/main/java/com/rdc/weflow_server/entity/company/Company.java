package com.rdc.weflow_server.entity.company;

import com.rdc.weflow_server.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

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
        if (businessNumber != null) this.businessNumber = businessNumber;
        if (representative != null) this.representative = representative;
        if (email != null) this.email = email;
        if (address != null) this.address = address;
        if (memo != null) this.memo = memo;
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
}