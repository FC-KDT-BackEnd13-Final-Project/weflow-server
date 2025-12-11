package com.rdc.weflow_server.entity.step;

import com.rdc.weflow_server.entity.BaseEntity;
import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.project.ProjectPhase;
import com.rdc.weflow_server.entity.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "steps")
@SQLDelete(sql = "update steps set deleted_at = now() where id = ?")
@Where(clause = "deleted_at is null")
public class Step extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ProjectPhase phase; // 상위 카테고리

    @Column(nullable = false)
    private String title; // 단계 이름(요구사항, 기획, 디자인, 퍼블리싱, 개발 등)

    @Column
    private String description; // 설명

    @Column
    private Integer orderIndex; // 정렬 순서

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private StepStatus status; // 단계 진행 상태

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project; // 프로젝트 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User createdBy; // 단계 생성자

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public void updateStatus(StepStatus status) {
        this.status = status;
    }
}
