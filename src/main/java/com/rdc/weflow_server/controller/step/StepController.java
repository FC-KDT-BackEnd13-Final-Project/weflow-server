package com.rdc.weflow_server.controller.step;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.dto.step.StepListResponse;
import com.rdc.weflow_server.dto.step.StepResponse;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import com.rdc.weflow_server.service.step.StepService;
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
public class StepController {

    private final StepService stepService;

    // 프로젝트 단계 목록 조회
    @GetMapping(value = "/projects/{projectId}/steps")
    public ApiResponse<StepListResponse> getStepsByProject(@PathVariable Long projectId,
                                                           @RequestParam(name = "phase", required = false) ProjectStatus phase) {
        StepListResponse response = (phase != null)
                ? stepService.getStepsByProject(projectId, phase)
                : stepService.getStepsByProject(projectId);
        return ApiResponse.success("step.list.success", response);
    }

    // 단일 단계 조회
    @GetMapping("/steps/{stepId}")
    public ApiResponse<StepResponse> getStep(@PathVariable Long stepId) {
        StepResponse response = stepService.getStep(stepId);
        return ApiResponse.success("step.get.success", response);
    }
}
