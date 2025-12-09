package com.rdc.weflow_server.dto.attachment;

import com.rdc.weflow_server.entity.attachment.Attachment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttachmentSimpleResponse {

    private Long id;
    private String fileName;
    private String url;
    private boolean isLink;

    public static AttachmentSimpleResponse from(Attachment attachment, String url) {
        return AttachmentSimpleResponse.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .url(url)
                .isLink(attachment.getAttachmentType() == Attachment.AttachmentType.LINK)
                .build();
    }
}
