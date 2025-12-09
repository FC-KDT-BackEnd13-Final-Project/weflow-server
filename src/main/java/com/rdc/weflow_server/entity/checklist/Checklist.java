package com.rdc.weflow_server.entity.checklist;

import com.rdc.weflow_server.entity.BaseEntity;
import com.rdc.weflow_server.entity.step.Step;
import com.rdc.weflow_server.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@Table(name = "checklists")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Checklist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 체크리스트 제목 */
    @Column(nullable = false, length = 255)
    private String title;

    /** 설명 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 템플릿 여부 */
    @Column(nullable = false)
    private Boolean isTemplate;

    /** 어떤 단계에 속한 체크리스트인지 (템플릿이면 NULL) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id")
    private Step step;

    /** 답변 제출 시 잠김 */
    @Column(nullable = false)
    private Boolean isLocked = false;

    /** 앱 종류/쇼핑몰/ERP 등 카테고리 */
    @Column(length = 50)
    private String category;

    /** 질문 리스트 */
    @OneToMany(mappedBy = "checklist", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChecklistQuestion> questions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // 체크리스트 수정
    public void updateChecklist(String title, String description) {
        if (this.isLocked) {
            throw new IllegalStateException("잠긴 체크리스트는 수정할 수 없습니다.");
        }

        if (title != null) this.title = title;
        if (description != null) this.description = description;
    }

    // 체크리스트 단계 수정
    public void changeStep(Step step) {
        this.step = step;
    }

    // 체크리스트 잠금
    public void lockChecklist() {
        this.isLocked = true;
    }

    // 템플릿 수정
    public void updateTemplate(String title, String description, String category) {
        if (this.isLocked) {
            throw new IllegalStateException("템플릿은 잠겨 있어 수정할 수 없습니다.");
        }

        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (category != null) this.category = category;
    }
}
