package com.rdc.weflow_server.controller.step;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.step.StepRequestAnswerCreateRequest;
import com.rdc.weflow_server.dto.step.StepRequestAnswerResponse;
import com.rdc.weflow_server.service.step.StepRequestAnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api")
public class StepRequestAnswerController {

    private final StepRequestAnswerService stepRequestAnswerService;

    @PostMapping("/requests/{requestId}/feedback")
    public ApiResponse<StepRequestAnswerResponse> answerRequest(@PathVariable Long requestId,
                                                                @AuthenticationPrincipal CustomUserDetails user,
                                                                @RequestBody @Valid StepRequestAnswerCreateRequest request) {
        StepRequestAnswerResponse response = stepRequestAnswerService.answerRequest(requestId, user.getId(), request);
        return ApiResponse.success("stepRequestAnswer.create.success", response);
    }

    @GetMapping("/requests/{requestId}/feedback")
    public ApiResponse<StepRequestAnswerResponse> getAnswer(@PathVariable Long requestId) {
        StepRequestAnswerResponse response = stepRequestAnswerService.getAnswer(requestId);
        return ApiResponse.success("stepRequestAnswer.get.success", response);
    }
}
