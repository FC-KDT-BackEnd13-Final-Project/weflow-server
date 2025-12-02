package com.rdc.weflow_server.entity.checklist;

import com.rdc.weflow_server.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@Table(name = "checklist_questions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistQuestion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 어떤 체크리스트인지 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id", nullable = false)
    private Checklist checklist;

    /** 질문 내용 */
    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    /** 질문 유형 (단일, 복수, 텍스트) */
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 20)
    private QuestionType questionType;

    /** 표시 순서 */
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    /** 보기 리스트 */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChecklistOption> options = new ArrayList<>();


    /** ENUM 정의 */
    public enum QuestionType {
        SINGLE,   // 단일 선택
        MULTI,    // 복수 선택
        TEXT      // 주관식 입력
    }

    // 질문 수정
    public void updateQuestion(String questionText, QuestionType questionType, Integer orderIndex) {
        if (questionText != null) this.questionText = questionText;
        if (questionType != null) this.questionType = questionType;
        if (orderIndex != null) this.orderIndex = orderIndex;
    }
}
