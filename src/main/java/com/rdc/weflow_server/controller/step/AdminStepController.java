package com.rdc.weflow_server.controller.step;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.step.StepCreateRequest;
import com.rdc.weflow_server.dto.step.StepListResponse;
import com.rdc.weflow_server.dto.step.StepReorderRequest;
import com.rdc.weflow_server.dto.step.StepResponse;
import com.rdc.weflow_server.dto.step.StepUpdateRequest;
import com.rdc.weflow_server.service.step.StepService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/admin")
public class AdminStepController {

    private final StepService stepService;

    @PostMapping("/projects/{projectId}/steps")
    public ApiResponse<StepResponse> createStep(@PathVariable Long projectId,
                                                @AuthenticationPrincipal CustomUserDetails user,
                                                @RequestBody @Valid StepCreateRequest request) {
        StepResponse response = stepService.createStep(projectId, user.getId(), request);
        return ApiResponse.success("step.create.success", response);
    }

    @GetMapping("/projects/{projectId}/steps")
    public ApiResponse<StepListResponse> getSteps(@PathVariable Long projectId) {
        StepListResponse response = stepService.getStepsByProject(projectId);
        return ApiResponse.success("step.list.success", response);
    }

    @GetMapping("/steps/{stepId}")
    public ApiResponse<StepResponse> getStep(@PathVariable Long stepId) {
        StepResponse response = stepService.getStep(stepId);
        return ApiResponse.success("step.get.success", response);
    }

    @PatchMapping("/steps/{stepId}")
    public ApiResponse<StepResponse> updateStep(@PathVariable Long stepId,
                                                @AuthenticationPrincipal CustomUserDetails user,
                                                @RequestBody @Valid StepUpdateRequest request) {
        StepResponse response = stepService.updateStep(stepId, user.getId(), request);
        return ApiResponse.success("step.update.success", response);
    }

    @DeleteMapping("/steps/{stepId}")
    public ApiResponse<Void> deleteStep(@PathVariable Long stepId,
                                        @AuthenticationPrincipal CustomUserDetails user) {
        stepService.deleteStep(stepId, user.getId());
        return ApiResponse.success("step.delete.success", null);
    }

    @PatchMapping("/steps/reorder")
    public ApiResponse<Void> reorderSteps(@AuthenticationPrincipal CustomUserDetails user,
                                          @RequestBody @Valid StepReorderRequest request) {
        stepService.reorderSteps(request.getProjectId(), user.getId(), request);
        return ApiResponse.success("step.reorder.success", null);
    }
}
