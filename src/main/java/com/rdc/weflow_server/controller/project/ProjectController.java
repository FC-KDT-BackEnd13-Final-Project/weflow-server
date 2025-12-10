package com.rdc.weflow_server.controller.project;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.project.response.ProjectDetailResponse;
import com.rdc.weflow_server.dto.project.response.ProjectListResponse;
import com.rdc.weflow_server.dto.project.response.ProjectSummaryResponse;
import com.rdc.weflow_server.service.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // 내 프로젝트 조회
    @GetMapping("/my")
    public ApiResponse<ProjectListResponse> getMyProjects(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ProjectListResponse projects =
                projectService.getMyProjects(user, keyword, page, size);

        return ApiResponse.success("MY_PROJECT_LIST_FETCHED", projects);
    }

    // 프로젝트 상세 조회
    @GetMapping("/{projectId}")
    public ApiResponse<ProjectDetailResponse> getProjectDetail(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ProjectDetailResponse dto = projectService.getProjectDetails(projectId, user);

        return ApiResponse.success("PROJECT_DETAIL_FETCHED", dto);
    }
}
