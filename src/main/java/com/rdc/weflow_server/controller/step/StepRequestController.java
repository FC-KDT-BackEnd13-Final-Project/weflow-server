package com.rdc.weflow_server.controller.step;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.step.StepRequestCreateRequest;
import com.rdc.weflow_server.dto.step.StepRequestListResponse;
import com.rdc.weflow_server.dto.step.StepRequestResponse;
import com.rdc.weflow_server.dto.step.StepRequestUpdateRequest;
import com.rdc.weflow_server.entity.step.StepRequestStatus;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.service.log.AuditContext;
import com.rdc.weflow_server.service.step.StepRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api")
public class StepRequestController {

    private final StepRequestService stepRequestService;

    @PostMapping("/steps/{stepId}/requests")
    public ApiResponse<StepRequestResponse> createRequest(@PathVariable Long stepId,
                                                          @AuthenticationPrincipal CustomUserDetails user,
                                                          @RequestBody @Valid StepRequestCreateRequest request,
                                                          HttpServletRequest httpRequest) {
        AuditContext ctx = new AuditContext(user.getId(), httpRequest.getRemoteAddr(), null);
        StepRequestResponse response = stepRequestService.createRequest(stepId, ctx, request);
        return ApiResponse.success("stepRequest.create.success", response);
    }

    @GetMapping("/steps/{stepId}/requests")
    public ApiResponse<StepRequestListResponse> getRequestsByStep(@PathVariable Long stepId,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size,
                                                                  @AuthenticationPrincipal CustomUserDetails user) {
        StepRequestListResponse response = stepRequestService.getRequestsByStep(stepId, page, size, user);
        return ApiResponse.success("stepRequest.listByStep.success", response);
    }

    @GetMapping("/projects/{projectId}/requests")
    public ApiResponse<StepRequestListResponse> getRequestsByProject(@PathVariable Long projectId,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size,
                                                                     @AuthenticationPrincipal CustomUserDetails user) {
        StepRequestListResponse response = stepRequestService.getRequestsByProject(projectId, page, size, user);
        return ApiResponse.success("stepRequest.listByProject.success", response);
    }

    @GetMapping("/requests/my")
    public ApiResponse<StepRequestListResponse> getMyProjectRequests(@AuthenticationPrincipal CustomUserDetails user,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size,
                                                                     @RequestParam(required = false) StepRequestStatus status) {
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        StepRequestListResponse response = stepRequestService.getRequestsByMyProjects(user.getId(), page, size, status);
        return ApiResponse.success("stepRequest.listByMyProjects.success", response);
    }

    @GetMapping("/requests/{requestId}")
    public ApiResponse<StepRequestResponse> getRequest(@PathVariable Long requestId) {
        StepRequestResponse response = stepRequestService.getRequest(requestId);
        return ApiResponse.success("stepRequest.get.success", response);
    }

    @PatchMapping("/requests/{requestId}")
    public ApiResponse<StepRequestResponse> updateRequest(@PathVariable Long requestId,
                                                          @AuthenticationPrincipal CustomUserDetails user,
                                                          @RequestBody @Valid StepRequestUpdateRequest request,
                                                          HttpServletRequest httpRequest) {
        AuditContext ctx = new AuditContext(user.getId(), httpRequest.getRemoteAddr(), null);
        StepRequestResponse response = stepRequestService.updateRequest(requestId, ctx, request);
        return ApiResponse.success("stepRequest.update.success", response);
    }

    @DeleteMapping("/requests/{requestId}")
    public ApiResponse<Void> cancelRequest(@PathVariable Long requestId,
                                           @AuthenticationPrincipal CustomUserDetails user,
                                           HttpServletRequest httpRequest) {
        AuditContext ctx = new AuditContext(user.getId(), httpRequest.getRemoteAddr(), null);
        stepRequestService.cancelRequest(requestId, ctx);
        return ApiResponse.success("stepRequest.cancel.success", null);
    }
}
