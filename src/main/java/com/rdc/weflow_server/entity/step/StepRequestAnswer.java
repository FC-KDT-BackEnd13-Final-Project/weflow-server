package com.rdc.weflow_server.entity.step;

import com.rdc.weflow_server.entity.BaseEntity;
import com.rdc.weflow_server.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@Table(name = "step_request_answer")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class StepRequestAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 응답 종류: APPROVE, REJECT, CHANGE_REQUEST */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ResponseType response;

    /** 사유 (REJECT, CHANGE_REQUEST 시 주로 사용) */
    @Column(columnDefinition = "TEXT")
    private String reasonText;

    /** 어떤 승인요청에 대한 응답인지 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_request_id", nullable = false)
    private StepRequest stepRequest;

    /** 응답자 (관리자 또는 고객사 담당자) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responded_by", nullable = false)
    private User respondedBy;

    // ENUM
    public enum ResponseType {
        APPROVE,
        REJECT,
        CHANGE_REQUEST
    }
}
