package com.rdc.weflow_server.entity.checklist;

import com.rdc.weflow_server.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@Table(name = "checklist_options")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 어떤 질문의 옵션인지 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private ChecklistQuestion question;

    /** 보기 내용 */
    @Column(name = "option_text", nullable = false, length = 255)
    private String optionText;

    /** 기타(직접입력) 입력란 여부 */
    @Column(name = "has_input", nullable = false)
    private Boolean hasInput;

    /** 보기 표시 순서 */
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    // 옵션 수정
    public void updateOption(String optionText, Boolean hasInput, Integer orderIndex) {
        if (optionText != null) this.optionText = optionText;
        if (hasInput != null) this.hasInput = hasInput;
        if (orderIndex != null) this.orderIndex = orderIndex;
    }
}
