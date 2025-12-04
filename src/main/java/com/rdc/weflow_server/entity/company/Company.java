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
    public void updateCompany(String name, String representative, String address, String memo, CompanyStatus status) {
        if (name != null) this.name = name;
        if (representative != null) this.representative = representative;
        if (address != null) this.address = address;
        if (memo != null) this.memo = memo;
        if (status != null) this.status = status;
    }
}