package com.rdc.weflow_server.dto.step;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StepReorderRequest {

    private Long projectId;
    private List<StepOrderItem> steps;
}
