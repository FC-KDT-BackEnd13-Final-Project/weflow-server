package com.rdc.weflow_server.dto.step;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import jakarta.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StepRequestCreateRequest {

    @NotBlank
    private String title;
    private String description;
    private List<Long> attachmentIds;
}
