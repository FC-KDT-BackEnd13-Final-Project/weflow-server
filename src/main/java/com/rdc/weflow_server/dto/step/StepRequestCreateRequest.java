package com.rdc.weflow_server.dto.step;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StepRequestCreateRequest {

    private String requestTitle;
    private String requestDescription;
    private List<Long> attachmentIds;
}
