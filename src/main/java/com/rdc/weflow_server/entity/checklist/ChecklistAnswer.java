package com.rdc.weflow_server.entity.checklist;

import com.rdc.weflow_server.entity.BaseEntity;
import com.rdc.weflow_server.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@Entity
@Table(name = "checklist_answers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 어떤 질문에 대한 답변인지 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private ChecklistQuestion question;

    /** 선택된 옵션 (객관식) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private ChecklistOption selectedOption;

    /** 주관식 입력 또는 ‘기타’ 입력 */
    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    /** 답변 작성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answered_by", nullable = false)
    private User answeredBy;

    /** 답변 작성 시각 */
    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;
}
