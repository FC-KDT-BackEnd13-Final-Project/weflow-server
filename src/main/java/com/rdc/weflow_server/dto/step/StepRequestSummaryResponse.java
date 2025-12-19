package com.rdc.weflow_server.dto.step;

import com.rdc.weflow_server.entity.project.ProjectPhase;
import com.rdc.weflow_server.entity.step.StepRequest;
import com.rdc.weflow_server.entity.step.StepRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class StepRequestSummaryResponse {

    private Long id;
    private String title;
    private StepRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;
    private LocalDateTime updatedAt;
    private Long projectId;
    private String projectName;
    private ProjectPhase phase;
    private Long stepId;
    private String stepTitle;
    private Long requestedBy;
    private String requestedByName;
    private boolean hasAttachment;

    public static StepRequestSummaryResponse from(StepRequest sr) {
        return StepRequestSummaryResponse.builder()
                .id(sr.getId())
                .title(sr.getRequestTitle())
                .status(sr.getStatus())
                .createdAt(sr.getCreatedAt())
                .decidedAt(sr.getDecidedAt())
                .updatedAt(sr.getUpdatedAt())
                .projectId(sr.getStep().getProject().getId())
                .projectName(sr.getStep().getProject().getName())
                .phase(sr.getStep().getPhase())
                .stepId(sr.getStep().getId())
                .stepTitle(sr.getStep().getTitle())
                .requestedBy(sr.getRequestedBy().getId())
                .requestedByName(sr.getRequestedBy().getName())
                .hasAttachment(false)
                .build();
    }

}
