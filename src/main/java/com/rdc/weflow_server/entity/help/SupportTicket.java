package com.rdc.weflow_server.entity.help;

import com.rdc.weflow_server.entity.BaseEntity;
import com.rdc.weflow_server.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@Table(name = "support_tickets")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SupportTicket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 제목 */
    @Column(nullable = false, length = 255)
    private String title;

    /** 내용 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 상태: OPEN, IN_PROGRESS, RESOLVED, CLOSED */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status;

    /** 티켓 종료일 */
    @Column(name = "closed_at")
    private java.time.LocalDateTime closedAt;

    /** 작성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public enum Status {
        OPEN,
        IN_PROGRESS,
        RESOLVED,
        CLOSED
    }
}
