package com.rdc.weflow_server.dto.step;

import com.rdc.weflow_server.entity.step.Phase;
import com.rdc.weflow_server.entity.step.StepStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StepCreateRequest {

    private String title;
    private String description;
    private Phase phase;
    private Integer orderIndex;
    private StepStatus status;
}
