package com.rdc.weflow_server.dto.step;

import com.rdc.weflow_server.entity.project.ProjectPhase;
import com.rdc.weflow_server.entity.step.StepStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class StepResponse {

    private Long id;
    private ProjectPhase phase;
    private String title;
    private String description;
    private Integer orderIndex;
    private StepStatus status;
    private Long projectId;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
