package com.rdc.weflow_server.dto.attachment;

import com.rdc.weflow_server.entity.attachment.Attachment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AttachmentResponse {

    private Long id;
    private Attachment.TargetType targetType;
    private Long targetId;
    private Attachment.AttachmentType attachmentType;
    private String filePath; // 첨부 파일의 경로
    private String fileName;
    private Long fileSize;
    private String contentType;
    private String url; // 첨부 link의 url
    private LocalDateTime createdAt;

    public static AttachmentResponse from(Attachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .targetType(attachment.getTargetType())
                .targetId(attachment.getTargetId())
                .attachmentType(attachment.getAttachmentType())
                .filePath(attachment.getFilePath())
                .fileName(attachment.getFileName())
                .fileSize(attachment.getFileSize())
                .contentType(attachment.getContentType())
                .url(attachment.getUrl())
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}
