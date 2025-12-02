package com.rdc.weflow_server.dto.step;

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
    private Long stepId;
    private String stepTitle;
}
