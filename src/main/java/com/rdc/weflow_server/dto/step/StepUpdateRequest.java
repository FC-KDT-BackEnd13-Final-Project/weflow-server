package com.rdc.weflow_server.dto.step;

import com.rdc.weflow_server.entity.step.Phase;
import com.rdc.weflow_server.entity.step.StepStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StepUpdateRequest {

    private String title;
    private String description;
    private Phase phase;
    private StepStatus status;
}
