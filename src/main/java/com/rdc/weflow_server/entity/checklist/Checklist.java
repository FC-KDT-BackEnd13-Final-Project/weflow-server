package com.rdc.weflow_server.entity.checklist;

import com.rdc.weflow_server.entity.BaseEntity;
import com.rdc.weflow_server.entity.step.Step;
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

    /** 어떤 템플릿에서 복사되었는지 (템플릿이면 NULL) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private Checklist template;

    /** 답변 제출 시 잠김 */
    @Column(nullable = false)
    private Boolean isLocked = false;

    /** 앱 종류/쇼핑몰/ERP 등 카테고리 */
    @Column(length = 50)
    private String category;

    /** 질문 리스트 */
    @OneToMany(mappedBy = "checklist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChecklistQuestion> questions = new ArrayList<>();
}
