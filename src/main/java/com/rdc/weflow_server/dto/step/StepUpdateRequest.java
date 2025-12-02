package com.rdc.weflow_server.dto.step;

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
}
