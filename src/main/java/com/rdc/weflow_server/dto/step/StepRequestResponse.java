package com.rdc.weflow_server.dto.step;

import com.rdc.weflow_server.entity.step.StepRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;
}
