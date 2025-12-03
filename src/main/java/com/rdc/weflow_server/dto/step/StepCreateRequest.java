package com.rdc.weflow_server.dto.step;

import com.rdc.weflow_server.entity.project.ProjectStatus;
import com.rdc.weflow_server.entity.step.StepStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StepCreateRequest {

    @NotBlank
    private String title;
    private String description;
    private Integer orderIndex;
    private StepStatus status;
    private ProjectStatus phase;
}
