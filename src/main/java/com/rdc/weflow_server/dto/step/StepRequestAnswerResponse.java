package com.rdc.weflow_server.dto.step;

import com.rdc.weflow_server.entity.step.StepRequestAnswerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class StepRequestAnswerResponse {

    private Long id;
    private StepRequestAnswerType response;
    private Long requestId;
    private Long respondedBy;
    private LocalDateTime createdAt;
}
