package com.rdc.weflow_server.controller.project;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.project.request.ProjectRoleUpdateRequest;
import com.rdc.weflow_server.dto.project.response.ProjectMemberResponse;
import com.rdc.weflow_server.dto.project.response.ProjectRoleUpdateResponse;
import com.rdc.weflow_server.service.project.ProjectMemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    /** 프로젝트 멤버 리스트 조회 **/
    @GetMapping("/{projectId}/members")
    public ApiResponse<List<ProjectMemberResponse>> getProjectMembers(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<ProjectMemberResponse> members =
                projectMemberService.getProjectMembers(
                        projectId,
                        user.getId()
                );

        return ApiResponse.success("PROJECT_MEMBERS_FETCHED", members);
    }

    /** AGENCY + ADMIN 만 projectRole 권한 변경 가능 **/
    @PatchMapping("/{projectId}/members/{memberId}/role")
    public ApiResponse<ProjectRoleUpdateResponse> updateProjectMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @RequestBody ProjectRoleUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest
    ) {
        ProjectRoleUpdateResponse result = projectMemberService.updateProjectRole(
                projectId,
                memberId,
                user.getId(),
                user.getRole(),
                request.getProjectRole(),
                servletRequest.getRemoteAddr()
        );

        return ApiResponse.success("PROJECT_ROLE_UPDATED", result);
    }

    /** AGENCY + ADMIN 만 projectRole 권한 삭제 가능 **/
    @DeleteMapping("/{projectId}/members/{memberId}")
    public ApiResponse<Void> removeProjectMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest
    ) {
        projectMemberService.removeProjectMember(
                projectId,
                memberId,
                user.getId(),
                user.getRole(),
                servletRequest.getRemoteAddr()
        );

        return ApiResponse.success("PROJECT_MEMBER_REMOVED", null);
    }
}
