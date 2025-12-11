package com.rdc.weflow_server.entity.project;

import com.rdc.weflow_server.entity.BaseEntity;
import com.rdc.weflow_server.entity.company.Company;
import com.rdc.weflow_server.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@Table(name = "projects")
@Entity
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ProjectPhase phase;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @Column
    private LocalDateTime startDate; // 프로젝트 시작일 (생성일과 다름)

    @Column
    private LocalDateTime endDate; // 프로젝트 종료일

    @Column
    private LocalDateTime expectedEndDate; // 프로젝트 예상 종료일

    @Column
    private BigDecimal contractPrice; // 계약 금액

    @Column
    private String contractFileUrl; // 계약서 파일 경로

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    // 생성자 User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User createdUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "project")
    private List<ProjectMember> projectMembers;

    public void updateProject(
            String name,
            String description,
            ProjectPhase phase,
            ProjectStatus status,
            LocalDateTime startDate,
            LocalDateTime endDateExpected,
            LocalDateTime endDate,
            BigDecimal contractPrice,
            String contractFileUrl,
            Company company
    ) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (phase != null) this.phase = phase;
        if (status != null) this.status = status;
        if (startDate != null) this.startDate = startDate;
        if (endDateExpected != null) this.expectedEndDate = endDateExpected;
        if (endDate != null) this.endDate = endDate;
        if (contractPrice != null) this.contractPrice = contractPrice;
        if (contractFileUrl != null) this.contractFileUrl = contractFileUrl;
        if (company != null) this.company = company;
    }
}
