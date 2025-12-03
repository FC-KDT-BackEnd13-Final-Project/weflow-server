package com.rdc.weflow_server.controller.step;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.step.StepRequestCreateRequest;
import com.rdc.weflow_server.dto.step.StepRequestListResponse;
import com.rdc.weflow_server.dto.step.StepRequestResponse;
import com.rdc.weflow_server.dto.step.StepRequestUpdateRequest;
import com.rdc.weflow_server.service.step.StepRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api")
public class StepRequestController {

    private final StepRequestService stepRequestService;

    @PostMapping("/steps/{stepId}/requests")
    public ApiResponse<StepRequestResponse> createRequest(@PathVariable Long stepId,
                                                          @AuthenticationPrincipal CustomUserDetails user,
                                                          @RequestBody @Valid StepRequestCreateRequest request) {
        StepRequestResponse response = stepRequestService.createRequest(stepId, user.getId(), request);
        return ApiResponse.success("stepRequest.create.success", response);
    }

    @GetMapping("/steps/{stepId}/requests")
    public ApiResponse<StepRequestListResponse> getRequestsByStep(@PathVariable Long stepId) {
        StepRequestListResponse response = stepRequestService.getRequestsByStep(stepId);
        return ApiResponse.success("stepRequest.listByStep.success", response);
    }

    @GetMapping("/projects/{projectId}/requests")
    public ApiResponse<StepRequestListResponse> getRequestsByProject(@PathVariable Long projectId) {
        StepRequestListResponse response = stepRequestService.getRequestsByProject(projectId);
        return ApiResponse.success("stepRequest.listByProject.success", response);
    }

    @GetMapping("/requests/{requestId}")
    public ApiResponse<StepRequestResponse> getRequest(@PathVariable Long requestId) {
        StepRequestResponse response = stepRequestService.getRequest(requestId);
        return ApiResponse.success("stepRequest.get.success", response);
    }

    @PatchMapping("/requests/{requestId}")
    public ApiResponse<StepRequestResponse> updateRequest(@PathVariable Long requestId,
                                                          @AuthenticationPrincipal CustomUserDetails user,
                                                          @RequestBody @Valid StepRequestUpdateRequest request) {
        StepRequestResponse response = stepRequestService.updateRequest(requestId, user.getId(), request);
        return ApiResponse.success("stepRequest.update.success", response);
    }

    @DeleteMapping("/requests/{requestId}")
    public ApiResponse<Void> cancelRequest(@PathVariable Long requestId,
                                           @AuthenticationPrincipal CustomUserDetails user) {
        stepRequestService.cancelRequest(requestId, user.getId());
        return ApiResponse.success("stepRequest.cancel.success", null);
    }
}
