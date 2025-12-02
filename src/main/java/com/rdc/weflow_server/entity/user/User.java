package com.rdc.weflow_server.entity.user;

import com.rdc.weflow_server.entity.BaseEntity;
import com.rdc.weflow_server.entity.company.Company;
import com.rdc.weflow_server.entity.project.ProjectMember;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@Entity
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Email
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column
    private String phoneNumber;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column
    @Builder.Default
    private Boolean isTemporaryPassword = true;

    @Column
    private LocalDateTime lastLoginAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "user")
    private List<ProjectMember> projectMembers;

    // 로그인 성공 시 시간 업데이트
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }
}