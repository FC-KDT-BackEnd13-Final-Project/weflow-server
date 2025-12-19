package com.rdc.weflow_server.dto.step;

import com.rdc.weflow_server.dto.attachment.AttachmentSimpleResponse;
import com.rdc.weflow_server.entity.step.StepRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class StepRequestResponse {

    private Long id;
    private String title;
    private String description;
    private StepRequestStatus status;
    private LocalDateTime decidedAt;
    private Long stepId;
    private Long projectId;
    private Long requestedBy;
    private String requestedByName;
    private Long decidedBy;
    private String decidedByName;
    private String decisionReason;
    private List<AttachmentSimpleResponse> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
