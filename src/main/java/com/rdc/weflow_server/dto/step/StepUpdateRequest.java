package com.rdc.weflow_server.dto.step;

import com.rdc.weflow_server.entity.step.StepCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StepUpdateRequest {

    @NotBlank
    private String title;
    private String description;
    private StepCategory category;
}
