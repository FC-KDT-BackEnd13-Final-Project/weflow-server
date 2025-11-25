package com.rdc.weflow_server.entity.step;

import com.rdc.weflow_server.entity.BaseEntity;
import com.rdc.weflow_server.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Builder
@Entity
@Table(name = "step_request_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class StepRequestHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 변경 타입: REQUEST_UPDATE, FILE_UPDATE, REASON_UPDATE */
    @Enumerated(EnumType.STRING)
    @Column(name = "history_type", nullable = false, length = 30)
    private HistoryType historyType;

    /** 변경된 필드명 (예: request_title, request_description, reason 등) */
    @Column(name = "field_name", length = 100)
    private String fieldName;

    /** 변경되기 전 값(JSON, TEXT 등) */
    @Column(name = "before_content", columnDefinition = "TEXT")
    private String beforeContent;

    /** 이력이 만들어진 시점 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 어떤 승인요청의 이력인지 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private StepRequest request;

    /** 변경한 유저 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", nullable = false)
    private User updatedBy;

    // --- ENUM 정의 ---
    public enum HistoryType {
        REQUEST_UPDATE,
        FILE_UPDATE,
        REASON_UPDATE
    }
}
