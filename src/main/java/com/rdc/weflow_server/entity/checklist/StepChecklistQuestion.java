package com.rdc.weflow_server.entity.checklist;

import com.rdc.weflow_server.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@Table(name = "step_checklist_questions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class StepChecklistQuestion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 질문 내용 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    /** 질문 유형 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionType questionType;

    /** 순서 */
    @Column(nullable = false)
    private Integer orderIndex;

    /** 체크리스트 ID */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id", nullable = false)
    private StepChecklist checklist;

    public enum QuestionType {
        SINGLE,
        MULTI,
        TEXT
    }

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StepChecklistOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "question")
    private List<StepChecklistAnswer> answers = new ArrayList<>();
}
