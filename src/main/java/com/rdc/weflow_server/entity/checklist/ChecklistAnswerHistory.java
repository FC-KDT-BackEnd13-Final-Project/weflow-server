package com.rdc.weflow_server.entity.checklist;

import com.rdc.weflow_server.entity.BaseEntity;
import com.rdc.weflow_server.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@Table(name = "checklist_answer_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistAnswerHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 버전 번호 */
    @Column(nullable = false)
    private Integer version;

    /** 상태: YES, NO, REVISE, HOLD */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status;

    /** 코멘트/메모 */
    @Column(columnDefinition = "TEXT")
    private String memo;

    /** 첨부파일 목록(JSON 배열) */
    @Column(columnDefinition = "JSON")
    private String attachments;

    /** 수정 시각 */
    @Column(nullable = false)
    private java.time.LocalDateTime editedAt;

    /** 원본 체크리스트 답변 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_answer_id", nullable = false)
    private StepChecklistAnswer checklistAnswer;

    /** 수정자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edited_by", nullable = false)
    private User editedBy;

    // --- ENUM 정의 ---
    public enum Status {
        YES,
        NO,
        REVISE,
        HOLD
    }
}
