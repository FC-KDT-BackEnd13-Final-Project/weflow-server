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
}