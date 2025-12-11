package com.rdc.weflow_server.controller.checklist;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.checklist.request.ChecklistAnswerRequest;
import com.rdc.weflow_server.dto.checklist.request.ChecklistCreateRequest;
import com.rdc.weflow_server.dto.checklist.request.ChecklistUpdateRequest;
import com.rdc.weflow_server.dto.checklist.response.ChecklistDetailResponse;
import com.rdc.weflow_server.dto.checklist.response.ChecklistResponse;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.service.checklist.ChecklistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/checklists")
public class ChecklistController {
    private final ChecklistService checklistService;

    // 체크리스트 생성
    @PostMapping
    public ApiResponse<Long> createChecklist(
            @RequestBody ChecklistCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest) {
        Long id = checklistService.createChecklist(
                request,
                user.getUser(),
                servletRequest.getRemoteAddr()
        );
        return ApiResponse.success("CHECKLIST_CREATED", id);
    }

    // 프로젝트별 체크리스트 목록 조회
    @GetMapping
    public ApiResponse<Page<ChecklistResponse>> getProjectChecklists(
            @RequestParam Long projectId,
            Pageable pageable
    ) {
        Page<ChecklistResponse> data =
                checklistService.getProjectChecklists(projectId, pageable);
        return ApiResponse.success("PROJECT_CHECKLIST_LIST", data);
    }


    // 체크리스트 상세 조회
    @GetMapping("/{checklistId}")
    public ApiResponse<ChecklistDetailResponse> getChecklistDetail(
            @PathVariable Long checklistId
    ) {
        ChecklistDetailResponse response = checklistService.getChecklistDetail(checklistId);
        return ApiResponse.success("CHECKLIST_FETCHED", response);
    }

    // 체크리스트 수정
    @PatchMapping("/{checklistId}")
    public ApiResponse<Long> updateChecklist(
            @PathVariable Long checklistId,
            @RequestBody ChecklistUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest
    ) {
        Long id = checklistService.updateChecklist(
                checklistId,
                request,
                user.getUser(),
                servletRequest.getRemoteAddr());
        return ApiResponse.success("CHECKLIST_UPDATED", id);
    }

    // 체크리스트 삭제
    @DeleteMapping("/{checklistId}")
    public ApiResponse<Long> deleteChecklist(
            @PathVariable Long checklistId,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest
    ) {
        Long id = checklistService.deleteChecklist(
                checklistId,
                user.getUser(),
                servletRequest.getRemoteAddr());
        return ApiResponse.success("CHECKLIST_DELETED", id);
    }

    // 체크리스트 답변 제출
    @PostMapping("/answers")
    public ApiResponse<Long> submitChecklistAnswers(
            @RequestBody ChecklistAnswerRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest
    ) {
        Long id = checklistService.submitChecklistAnswers(
                request,
                user.getUser(),
                servletRequest.getRemoteAddr()
        );
        return ApiResponse.success("CHECKLIST_ANSWER_SUBMITTED", id);
    }
}
