package com.rdc.weflow_server.controller.project;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.project.request.AdminProjectCreateRequest;
import com.rdc.weflow_server.dto.project.request.AdminProjectMemberAddRequest;
import com.rdc.weflow_server.dto.project.request.AdminProjectUpdateRequest;
import com.rdc.weflow_server.dto.project.response.*;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import com.rdc.weflow_server.service.project.AdminProjectService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/projects")
@RequiredArgsConstructor
public class AdminProjectController {

    private final AdminProjectService adminProjectService;

    // 프로젝트 생성
    @PostMapping
    public ApiResponse<AdminProjectCreateResponse> createProject(
            @RequestBody AdminProjectCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest
    ) {
        AdminProjectCreateResponse response = adminProjectService.createProject(
                request,
                user,
                servletRequest.getRemoteAddr()
        );

        return ApiResponse.success(
                "PROJECT_CREATED", response);
    }

    // 프로젝트 목록 조회
    @GetMapping
    public ApiResponse<AdminProjectListResponse> getProjects(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        AdminProjectListResponse response = adminProjectService.getProjectList(status, companyId, keyword, page, size);
        return ApiResponse.success("PROJECT_LIST_FETCHED", response);
    }

    // 프로젝트 상세 조회
    @GetMapping("/{projectId}")
    public ApiResponse<AdminProjectDetailResponse> getProjectDetail(
            @PathVariable Long projectId
    ) {
        return  ApiResponse.success("PROJECT_DETAIL_FETCHED",
                adminProjectService.getProjectDetail(projectId));
    }

    // 프로젝트 수정
    @PatchMapping("/{projectId}")
    public ApiResponse<AdminProjectUpdateResponse> updateProject(
            @PathVariable Long projectId,
            @RequestBody AdminProjectUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest
    ) {
        AdminProjectUpdateResponse response = adminProjectService.updateProject(
                projectId,
                request,
                user,
                servletRequest.getRemoteAddr()
        );

        return ApiResponse.success(
                "PROJECT_UPDATED", response);
    }

    // 프로젝트 삭제
    @DeleteMapping("/{projectId}")
    public ApiResponse<Void> deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest
    ) {
        adminProjectService.deleteProject(projectId, user, servletRequest.getRemoteAddr());
        return ApiResponse.success("PROJECT_DELETED", null);
    }

    // 프로젝트 멤버 추가
    @PostMapping("/{projectId}/members")
    public ApiResponse<AdminProjectMemberAddResponse> addProjectMember(
            @PathVariable Long projectId,
            @RequestBody AdminProjectMemberAddRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest
    ) {
        AdminProjectMemberAddResponse response = adminProjectService.addProjectMember(
                projectId, request, user, servletRequest.getRemoteAddr());
        return ApiResponse.success("PROJECT_MEMBER_ADDED", response);
    }

    // 프로젝트 멤버 조회
    @GetMapping("/{projectId}/members")
    public ApiResponse<AdminProjectMemberListResponse> getProjectMembers(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.success(
                "PROJECT_MEMBER_LIST_FETCHED",
                adminProjectService.getProjectMembers(projectId, user)
        );
    }

    // 프로젝트 멤버 삭제
    @DeleteMapping("/{projectId}/members/{userId}")
    public ApiResponse<Void> removeProjectMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest
    ) {
        adminProjectService.removeProjectMember(projectId, userId, user, servletRequest.getRemoteAddr());
        return ApiResponse.success("PROJECT_MEMBER_REMOVED", null);
    }
}
