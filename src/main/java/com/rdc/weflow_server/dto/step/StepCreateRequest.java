package com.rdc.weflow_server.dto.step;

import com.rdc.weflow_server.entity.step.Phase;
import com.rdc.weflow_server.entity.step.StepStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StepCreateRequest {

    @NotBlank
    private String title;
    private String description;
    @NotNull
    private Phase phase;
    private Integer orderIndex;
    private StepStatus status;
}
