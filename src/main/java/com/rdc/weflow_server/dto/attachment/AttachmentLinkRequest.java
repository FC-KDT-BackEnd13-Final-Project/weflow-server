package com.rdc.weflow_server.dto.attachment;

import com.rdc.weflow_server.entity.attachment.Attachment;
import lombok.Getter;

@Getter
public class AttachmentLinkRequest {

    private Attachment.TargetType targetType; // 어떤 엔티티에 속하는 첨부인지 (Post, Step, Support)
    private Long targetId;
    private String url; // https://www.google.com
}
