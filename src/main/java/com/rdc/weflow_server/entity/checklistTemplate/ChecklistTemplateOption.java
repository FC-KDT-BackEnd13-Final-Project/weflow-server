package com.rdc.weflow_server.entity.checklistTemplate;

import com.rdc.weflow_server.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@Table(name = "checklist_template_options")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistTemplateOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 질문 FK */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private ChecklistTemplateQuestion question;

    /** 보기 내용 */
    @Column(nullable = false, length = 255)
    private String optionText;

    /** 직접 입력 가능 여부 (기타 입력칸) */
    @Builder.Default
    @Column(nullable = false)
    private Boolean hasInput = false;

    /** 보기 순서 */
    @Column(nullable = false)
    private Integer orderIndex;
}
