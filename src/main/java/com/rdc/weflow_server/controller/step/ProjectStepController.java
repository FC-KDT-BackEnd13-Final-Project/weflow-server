package com.rdc.weflow_server.controller.step;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.step.*;
import com.rdc.weflow_server.service.log.AuditContext;
import com.rdc.weflow_server.service.step.StepService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/projects")
public class ProjectStepController {

    private final StepService stepService;

    /** 단계 생성 */
    @PostMapping("/{projectId}/steps")
    public ApiResponse<StepResponse> createStep(@PathVariable Long projectId,
                                                @AuthenticationPrincipal CustomUserDetails user,
                                                @RequestBody @Valid StepCreateRequest request,
                                                HttpServletRequest httpRequest) {
        AuditContext ctx = new AuditContext(user.getId(), httpRequest.getRemoteAddr(), projectId);
        StepResponse response = stepService.createStep(projectId, request, ctx);
        return ApiResponse.success("step.create.success", response);
    }

    /** 단계 수정 */
    @PatchMapping("/steps/{stepId}")
    public ApiResponse<StepResponse> updateStep(@PathVariable Long stepId,
                                                @AuthenticationPrincipal CustomUserDetails user,
                                                @RequestBody @Valid StepUpdateRequest request,
                                                HttpServletRequest httpRequest) {
        AuditContext ctx = new AuditContext(user.getId(), httpRequest.getRemoteAddr(), null);
        StepResponse response = stepService.updateStep(stepId, request, ctx);
        return ApiResponse.success("step.update.success", response);
    }

    /** 단계 삭제 */
    @DeleteMapping("/steps/{stepId}")
    public ApiResponse<Void> deleteStep(@PathVariable Long stepId,
                                        @AuthenticationPrincipal CustomUserDetails user,
                                        HttpServletRequest httpRequest) {
        AuditContext ctx = new AuditContext(user.getId(), httpRequest.getRemoteAddr(), null);
        stepService.deleteStep(stepId, ctx);
        return ApiResponse.success("step.delete.success", null);
    }

    /** 단계 순서 재배치 */
    @PatchMapping("/{projectId}/steps/reorder")
    public ApiResponse<StepListResponse> reorderSteps(@AuthenticationPrincipal CustomUserDetails user,
                                                      @PathVariable Long projectId,
                                                      @RequestBody @Valid StepPhaseReorderRequest request,
                                                      HttpServletRequest httpRequest) {
        AuditContext ctx = new AuditContext(user.getId(), httpRequest.getRemoteAddr(), projectId);
        StepListResponse response = stepService.reorderSteps(projectId, request, ctx);
        return ApiResponse.success("step.reorder.success", response);
    }
}
