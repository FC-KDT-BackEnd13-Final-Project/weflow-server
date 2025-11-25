package com.rdc.weflow_server.entity.project;

import com.rdc.weflow_server.entity.BaseEntity;
import com.rdc.weflow_server.entity.company.Company;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@Table(name = "projects")
@Entity
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "project")
    private List<ProjectMember> projectMembers;

}
