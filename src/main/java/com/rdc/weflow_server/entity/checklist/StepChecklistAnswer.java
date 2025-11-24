package com.rdc.weflow_server.entity.checklist;

import com.rdc.weflow_server.entity.BaseEntity;
import com.rdc.weflow_server.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@Table(name = "step_checklist_answers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class StepChecklistAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 해당 질문 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private StepChecklistQuestion question;

    /** 선택한 보기 (객관식) — 주관식일 경우 NULL */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private StepChecklistOption selectedOption;

    /** 주관식 입력값 / 기타 입력 텍스트 */
    @Column(columnDefinition = "TEXT")
    private String answerText;

    /** 답변한 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User answeredBy;

    @OneToMany(mappedBy = "checklistAnswer", cascade = CascadeType.ALL)
    private List<ChecklistAnswerHistory> histories = new ArrayList<>();
}
