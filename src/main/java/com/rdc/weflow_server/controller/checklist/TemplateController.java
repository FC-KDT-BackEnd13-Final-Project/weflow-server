package com.rdc.weflow_server.controller.checklist;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.dto.checklist.request.ChecklistCreateRequest;
import com.rdc.weflow_server.dto.checklist.response.TemplateDetailResponse;
import com.rdc.weflow_server.dto.checklist.response.TemplateResponse;
import com.rdc.weflow_server.dto.checklist.request.TemplateRequest;
import com.rdc.weflow_server.service.checklist.ChecklistTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checklist-templates")
@RequiredArgsConstructor
public class TemplateController {
    private final ChecklistTemplateService checklistTemplateService;

    // 템플릿 생성
    @PostMapping
    public ApiResponse<Long> createTemplate(@RequestBody ChecklistCreateRequest request) {
        Long templateId = checklistTemplateService.createTemplate(request);
        return ApiResponse.success(
                "TEMPLATE_CREATED",
                templateId
        );
    }

    // 템플릿 목록 조회
    @GetMapping
    public ApiResponse<List<TemplateResponse>> getTemplateList() {
        return ApiResponse.success(
                "TEMPLATE_LIST_FETCHED",
                checklistTemplateService.getTemplateList()
        );
    }

    // 템플릿 상세 조회
    @GetMapping("/{templateId}")
    public ApiResponse<TemplateDetailResponse> getTemplateDetail(@PathVariable Long templateId) {
        return   ApiResponse.success(
                "TEMPLATE_FETCHED",
                checklistTemplateService.getTemplateDetail(templateId)
        );
    }

    // 템플릿 수정
    @PatchMapping("/{templateId}")
    public ApiResponse<Long> updateTemplate(@PathVariable Long templateId, @RequestBody TemplateRequest request) {
        Long id = checklistTemplateService.updateTemplate(templateId, request);

        return ApiResponse.success(
                "TEMPLATE_UPDATED",
                templateId
        );
    }

    // 템플릿 삭제
    @DeleteMapping("/{templateId}")
    public ApiResponse<Long> deleteTemplate(@PathVariable Long templateId) {
        Long id = checklistTemplateService.deleteTemplate(templateId);

        return ApiResponse.success(
                "TEMPLATE_DELETED",
                templateId
        );
    }
}
