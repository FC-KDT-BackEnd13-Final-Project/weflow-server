package com.rdc.weflow_server.dto.step;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class StepRequestListResponse {

    private Long totalCount;
    private int page;
    private int size;
    private List<StepRequestSummaryResponse> stepRequestSummaryResponses;
}
