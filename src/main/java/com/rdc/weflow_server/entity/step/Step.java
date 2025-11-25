package com.rdc.weflow_server.entity.step;

import com.rdc.weflow_server.entity.BaseEntity;
import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Table(name = "steps")
@Entity
public class Step extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Phase phase; // 상위 카테고리

    @Column(nullable = false)
    private String title; // 요구사항, 기획, 디자인, 퍼블리싱, 개발 ...

    @Column
    private String description;

    @Column
    private Integer orderIndex; // 정렬 순서

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private StepStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User createdBy;

}
