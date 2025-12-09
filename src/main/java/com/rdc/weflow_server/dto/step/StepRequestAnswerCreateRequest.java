package com.rdc.weflow_server.dto.step;

import com.rdc.weflow_server.entity.step.StepRequestAnswerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import jakarta.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StepRequestAnswerCreateRequest {

    private StepRequestAnswerType response;
    private String reasonText;
    @Size(max = 50)
    private List<Long> attachmentIds;
}
