package com.rdc.weflow_server.entity.step;

import com.rdc.weflow_server.entity.BaseEntity;
import com.rdc.weflow_server.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

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

    /** 버전 */
    @Column(nullable = false)
    private Integer version;

    /** 수정 시의 제목 스냅샷 */
    @Column(nullable = false, length = 255)
    private String title;

    /** 첨부파일 목록 JSON */
    @Column(columnDefinition = "JSON")
    private String attachments;

    /** 내용(코멘트/메모) 스냅샷 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 수정 시각 */
    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    /** 어떤 승인 요청의 이력인지 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private StepRequest request;

    /** 수정자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", nullable = false)
    private User updatedBy;
}
