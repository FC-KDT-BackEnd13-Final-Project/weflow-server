package com.rdc.weflow_server.entity.checklist;

import com.rdc.weflow_server.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@Table(name = "step_checklist_options")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class StepChecklistOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 보기 내용 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String optionText;

    /** 직접 입력 가능 여부 (기타 입력칸) */
    @Builder.Default
    @Column(nullable = false)
    private Boolean hasInput = false;

    /** 보기 순서 */
    @Column(nullable = false)
    private Integer orderIndex;

    /** 체크리스트 질문 ID */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private StepChecklistQuestion question;
}
