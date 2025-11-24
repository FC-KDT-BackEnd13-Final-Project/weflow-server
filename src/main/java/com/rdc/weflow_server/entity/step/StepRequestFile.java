package com.rdc.weflow_server.entity.step;

import com.rdc.weflow_server.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@Table(name = "step_request_files")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class StepRequestFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 파일 URL */
    @Column(name = "file_url", nullable = false, length = 255)
    private String fileUrl;

    /** 파일 이름 */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /** 어떤 승인요청에 속하는 파일인지 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_request_id", nullable = false)
    private StepRequest stepRequest;
}
