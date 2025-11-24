package com.rdc.weflow_server.entity.checklistTemplate;

import com.rdc.weflow_server.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@Table(name = "checklist_template_questions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistTemplateQuestion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 템플릿 FK */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ChecklistTemplate template;

    /** 질문 내용 */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String questionText;

    /** 질문 유형: SINGLE, MULTI, TEXT */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionType questionType;

    /** 템플릿 안에서의 순서 */
    @Column(nullable = false)
    private Integer orderIndex;

    // ENUM 정의
    public enum QuestionType {
        SINGLE,
        MULTI,
        TEXT
    }

    @Builder.Default
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChecklistTemplateOption> options = new ArrayList<>();
}
