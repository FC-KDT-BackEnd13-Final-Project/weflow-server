package com.rdc.weflow_server.controller.step;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.step.StepCreateRequest;
import com.rdc.weflow_server.dto.step.StepListResponse;
import com.rdc.weflow_server.dto.step.StepReorderRequest;
import com.rdc.weflow_server.dto.step.StepResponse;
import com.rdc.weflow_server.dto.step.StepUpdateRequest;
import com.rdc.weflow_server.entity.project.ProjectPhase;
import com.rdc.weflow_server.service.log.AuditContext;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/admin")
public class AdminStepController {

    private final StepService stepService;

    /**
     * Step list 조회
     */
    @GetMapping("/projects/{projectId}/steps")
    public ApiResponse<StepListResponse> getSteps(@PathVariable Long projectId,
                                                  @RequestParam(name = "phase", required = false) ProjectPhase phase) {
        StepListResponse response = (phase != null)
                ? stepService.getStepsByProject(projectId, phase)
                : stepService.getStepsByProject(projectId);
        return ApiResponse.success("step.list.success", response);
    }

    /**
     * Step 상세 조회
     */
    @GetMapping("/steps/{stepId}")
    public ApiResponse<StepResponse> getStep(@PathVariable Long stepId) {
        StepResponse response = stepService.getStep(stepId);
        return ApiResponse.success("step.get.success", response);
    }

    /**
     * Step 생성
     */
    @PostMapping("/projects/{projectId}/steps")
    public ApiResponse<StepResponse> createStep(@PathVariable Long projectId,
                                                @AuthenticationPrincipal CustomUserDetails user,
                                                @RequestBody @Valid StepCreateRequest request,
                                                HttpServletRequest httpRequest) {
        AuditContext ctx = new AuditContext(user.getId(), httpRequest.getRemoteAddr(), projectId);
        StepResponse response = stepService.createStep(projectId, request, ctx);
        return ApiResponse.success("step.create.success", response);
    }

    /**
     * Step 수정
     */
    @PatchMapping("/steps/{stepId}")
    public ApiResponse<StepResponse> updateStep(@PathVariable Long stepId,
                                                @AuthenticationPrincipal CustomUserDetails user,
                                                @RequestBody @Valid StepUpdateRequest request,
                                                HttpServletRequest httpRequest) {
        AuditContext ctx = new AuditContext(user.getId(), httpRequest.getRemoteAddr(), null);
        StepResponse response = stepService.updateStep(stepId, request, ctx);
        return ApiResponse.success("step.update.success", response);
    }

    /**
     * Step 삭제
     */
    @DeleteMapping("/steps/{stepId}")
    public ApiResponse<Void> deleteStep(@PathVariable Long stepId,
                                        @AuthenticationPrincipal CustomUserDetails user,
                                        HttpServletRequest httpRequest) {
        AuditContext ctx = new AuditContext(user.getId(), httpRequest.getRemoteAddr(), null);
        stepService.deleteStep(stepId, ctx);
        return ApiResponse.success("step.delete.success", null);
    }

    /**
     * Step 순서 바꾸기
     */
    @PatchMapping("/steps/reorder")
    public ApiResponse<Void> reorderSteps(@AuthenticationPrincipal CustomUserDetails user,
                                          @RequestBody @Valid StepReorderRequest request,
                                          HttpServletRequest httpRequest) {
        AuditContext ctx = new AuditContext(user.getId(), httpRequest.getRemoteAddr(), request.getProjectId());
        stepService.reorderSteps(request.getProjectId(), request, ctx);
        return ApiResponse.success("step.reorder.success", null);
    }
}
