package com.rdc.weflow_server.entity.step;

import com.rdc.weflow_server.entity.BaseEntity;
import com.rdc.weflow_server.entity.Step;
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
    private Long id;

    /** 요청 제목 */
    @Column(name = "request_title", nullable = false, length = 255)
    private String requestTitle;

    /** 요청 설명 */
    @Column(name = "request_description", columnDefinition = "TEXT")
    private String requestDescription;

    /** 승인 요청 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status;

    /** 승인/거절된 시각 */
    private LocalDateTime decidedAt;

    /** 어떤 단계(step)에 대한 승인요청인지 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private Step step;

    /** 승인 요청자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    // --- ENUM 정의 ---
    public enum Status {
        REQUESTED,
        APPROVED,
        REJECTED,
        CHANGE_REQUEST
    }

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StepRequestHistory> histories = new ArrayList<>();
}
