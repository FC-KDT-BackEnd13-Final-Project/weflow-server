package com.rdc.weflow_server.dto.attachment;

import com.rdc.weflow_server.entity.attachment.Attachment;
import lombok.Getter;

@Getter
public class AttachmentFileRequest {

    private Attachment.TargetType targetType;
    private Long targetId;
    private String filePath;
    private String fileName;
    private Long fileSize;
    private String contentType;
}
