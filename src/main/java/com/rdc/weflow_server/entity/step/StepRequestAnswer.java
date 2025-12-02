package com.rdc.weflow_server.entity.step;

import com.rdc.weflow_server.entity.BaseEntity;
import com.rdc.weflow_server.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@Table(name = "step_request_answers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class StepRequestAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    // 승인 응답 ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StepRequestAnswerType response;   // 응답 종류

    @Column(columnDefinition = "TEXT")
    private String reasonText;  // 사유

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_request_id", nullable = false)
    private StepRequest stepRequest;    // 승인 요청 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responded_by", nullable = false)
    private User respondedBy;   // 응답자 ID
}
