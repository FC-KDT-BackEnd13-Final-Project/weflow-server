package com.rdc.weflow_server.dto.step;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class StepRequestHistoryListResponse {

    private Long requestId;
    private List<StepRequestHistoryResponse> histories;
}
