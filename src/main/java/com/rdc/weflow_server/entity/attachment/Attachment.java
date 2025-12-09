package com.rdc.weflow_server.entity.attachment;

import com.rdc.weflow_server.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@Table(name = "attachments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Attachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 첨부 주체 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 30, nullable = false)
    private TargetType targetType;

    /** 첨부 주체 ID (post_id, comment_id, step_request_id 등) */
    @Column(name = "target_id")
    private Long targetId;

    /** 첨부 구분: FILE / LINK */
    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_type", length = 20, nullable = false)
    private AttachmentType attachmentType;

    /** 실제 저장 경로 (파일일 경우) */
    @Column(name = "file_path", length = 500)
    private String filePath; // S3 경로

    /** 원본 파일명 */
    @Column(name = "file_name")
    private String fileName;

    /** 파일 크기 */
    @Column(name = "file_size")
    private Long fileSize;

    /** 파일 MIME 타입 (파일일 경우) */
    @Column(name = "content_type", length = 100)
    private String contentType;

    /** 링크 주소 (링크 타입일 경우) */
    @Column(name = "url", length = 1000)
    private String url;

    public void bindTo(TargetType targetType, Long targetId) {
        this.targetType = targetType;
        this.targetId = targetId;
    }

    // --- ENUM 정의 ---

    /** 어떤 엔티티에 속하는 첨부인지 */
    public enum TargetType {
        POST,
        POST_COMMENT,
        STEP_REQUEST,
        STEP_REQUEST_ANSWER,
        SUPPORT,
        SUPPORT_COMMENT
    }

    /** 첨부 타입: 파일 or 링크 */
    public enum AttachmentType {
        FILE,
        LINK
    }
}
