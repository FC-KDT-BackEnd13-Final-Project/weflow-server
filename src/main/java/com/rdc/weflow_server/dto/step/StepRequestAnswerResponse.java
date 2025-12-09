package com.rdc.weflow_server.dto.step;

import com.rdc.weflow_server.dto.attachment.AttachmentSimpleResponse;
import com.rdc.weflow_server.entity.step.StepRequestAnswerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class StepRequestAnswerResponse {

    private Long id;
    private StepRequestAnswerType response;
    private Long requestId;
    private Long respondedBy;
    private String respondedByName;
    private String reasonText;
    private List<AttachmentSimpleResponse> attachments;
    private LocalDateTime decidedAt;
    private LocalDateTime createdAt;
}
