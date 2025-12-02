package com.rdc.weflow_server.controller.checklist;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.dto.checklist.request.ChecklistAnswerRequest;
import com.rdc.weflow_server.dto.checklist.request.ChecklistCreateRequest;
import com.rdc.weflow_server.dto.checklist.request.ChecklistUpdateRequest;
import com.rdc.weflow_server.dto.checklist.response.ChecklistDetailResponse;
import com.rdc.weflow_server.dto.checklist.response.ChecklistResponse;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.service.checklist.ChecklistService;
import lombok.RequiredArgsConstructor;
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
    public ApiResponse<Long> createChecklist(@RequestBody ChecklistCreateRequest request) {
        Long id = checklistService.createChecklist(request);
        return ApiResponse.success("CHECKLIST_CREATED", id);
    }

    // 프로젝트별 체크리스트 목록 조회
    @GetMapping
    public ApiResponse<List<ChecklistResponse>> getProjectChecklists(
            @RequestParam Long projectId
    ) {
        List<ChecklistResponse> list = checklistService.getProjectChecklists(projectId);
        return ApiResponse.success("PROJECT_CHECKLIST_LIST", list);
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
    public ApiResponse<Void> updateChecklist(
            @PathVariable Long checklistId,
            @RequestBody ChecklistUpdateRequest request
    ) {
        checklistService.updateChecklist(checklistId, request);
        return ApiResponse.success("CHECKLIST_UPDATED", null);
    }

    // 체크리스트 삭제
    @DeleteMapping("/{checklistId}")
    public ApiResponse<Void> deleteChecklist(@PathVariable Long checklistId) {
        checklistService.deleteChecklist(checklistId);
        return ApiResponse.success("CHECKLIST_DELETED", null);
    }

    // 체크리스트 답변 제출
    @PostMapping("/answers")
    public ApiResponse<Void> submitChecklistAnswers(
            @RequestBody ChecklistAnswerRequest request,
            @AuthenticationPrincipal User user
    ) {
        checklistService.submitChecklistAnswers(request, user);
        return ApiResponse.success("CHECKLIST_ANSWER_SUBMITTED", null);
    }
}
