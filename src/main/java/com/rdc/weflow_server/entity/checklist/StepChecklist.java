package com.rdc.weflow_server.entity.checklist;

import com.rdc.weflow_server.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@Table(name = "step_checklists")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class StepChecklist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 체크리스트 제목 (예: 요구사항 체크리스트) */
    @Column(nullable = false, length = 255)
    private String title;

    /** 설명 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 답변 제출되면 locked (TRUE) */
    @Builder.Default
    @Column(nullable = false)
    private Boolean isLocked = false;

    /** 어떤 단계(step)에 속했는지 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private Step step;  // ERD 확인 후 정확한 클래스명으로 변경 필요

    @OneToMany(mappedBy = "checklist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StepChecklistQuestion> questions = new ArrayList<>();
}
