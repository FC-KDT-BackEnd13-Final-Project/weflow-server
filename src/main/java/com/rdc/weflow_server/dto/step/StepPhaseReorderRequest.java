package com.rdc.weflow_server.dto.step;

import com.rdc.weflow_server.entity.project.ProjectPhase;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StepPhaseReorderRequest {

    @NotNull
    private ProjectPhase phase;

    @NotEmpty
    private List<@NotNull Long> orderedStepIds;
}
