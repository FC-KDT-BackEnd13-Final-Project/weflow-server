package com.rdc.weflow_server.dto.step;

import com.rdc.weflow_server.entity.step.StepRequestAnswerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StepRequestAnswerCreateRequest {

    private StepRequestAnswerType response;
    private String reasonText;
}
