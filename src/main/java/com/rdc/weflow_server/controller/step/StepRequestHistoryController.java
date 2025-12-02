package com.rdc.weflow_server.controller.step;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.dto.step.StepRequestHistoryListResponse;
import com.rdc.weflow_server.service.step.StepRequestHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api")
public class StepRequestHistoryController {

    private final StepRequestHistoryService stepRequestHistoryService;

    @GetMapping("/requests/{requestId}/histories")
    public ApiResponse<StepRequestHistoryListResponse> getHistories(@PathVariable Long requestId,
                                                                    @RequestParam(name = "sort", required = false, defaultValue = "asc") String sort) {
        StepRequestHistoryListResponse response = stepRequestHistoryService.getHistoriesByRequest(requestId, sort);
        return ApiResponse.success("stepRequestHistory.list.success", response);
    }
}
