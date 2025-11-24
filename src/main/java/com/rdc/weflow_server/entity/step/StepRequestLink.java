package com.rdc.weflow_server.entity.step;

import com.rdc.weflow_server.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@Table(name = "step_request_links")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class StepRequestLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 링크 URL */
    @Column(nullable = false, length = 255)
    private String url;

    /** 링크 표시 이름 */
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    /** 승인요청 ID */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_request_id", nullable = false)
    private StepRequest stepRequest;
}
