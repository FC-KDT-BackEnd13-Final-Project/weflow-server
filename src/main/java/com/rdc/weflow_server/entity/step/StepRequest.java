package com.rdc.weflow_server.entity.step;

import com.rdc.weflow_server.entity.BaseEntity;
import com.rdc.weflow_server.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@Table(name = "step_requests")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class StepRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    // 승인 요청 ID

    @Column(name = "request_title", nullable = false, length = 255)
    private String requestTitle;    // 요청 제목

    @Column(name = "request_description", columnDefinition = "TEXT")
    private String requestDescription;  // 요청 설명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StepRequestStatus status;  // 승인 요청 상태

    private LocalDateTime decidedAt;    // 승인/거절일

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private Step step;  // 단계 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;   // 승인 요청자 ID

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StepRequestHistory> histories = new ArrayList<>();

    public void updateStatus(StepRequestStatus status) {
        this.status = status;
    }

    public void updateDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }
}
