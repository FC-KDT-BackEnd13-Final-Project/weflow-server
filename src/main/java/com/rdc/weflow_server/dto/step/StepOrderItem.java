package com.rdc.weflow_server.dto.step;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StepOrderItem {

    private Long stepId;
    private Integer orderIndex;
}
