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

    /** 변경 타입: REQUEST_UPDATE(제목/설명), FILE_UPDATE(첨부 변경 예정), REASON_UPDATE(반려/승인 사유) */
    @Enumerated(EnumType.STRING)
    @Column(name = "history_type", nullable = false, length = 30)
    private HistoryType historyType;

    /** 변경된 필드명 (예: request_title, request_description, reason 등) */
    @Column(name = "field_name", length = 100)
    private String fieldName;

    /** 변경되기 전 값(JSON, TEXT 등) */
    @Column(name = "before_content", columnDefinition = "TEXT")
    private String beforeContent;

    /** 변경된 후 값(JSON, TEXT 등) */
    @Column(name = "after_content", columnDefinition = "TEXT")
    private String afterContent;

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
        // 제목/설명/상태 변경 내역: before/after에 텍스트/요약 저장
        REQUEST_UPDATE,
        // 첨부파일 변경 내역: TODO - 변경된 파일 목록 JSON 등으로 저장 예정
        FILE_UPDATE,
        // 승인/반려 사유 변경 내역: 사유 텍스트를 afterContent에 저장
        REASON_UPDATE
    }
}
