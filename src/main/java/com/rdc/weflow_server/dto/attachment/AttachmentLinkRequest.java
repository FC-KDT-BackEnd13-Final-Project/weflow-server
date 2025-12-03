package com.rdc.weflow_server.dto.attachment;

import com.rdc.weflow_server.entity.attachment.Attachment;
import lombok.Getter;

@Getter
public class AttachmentLinkRequest {

    private Attachment.TargetType targetType;
    private Long targetId;
    private String url;
}
