package com.rdc.weflow_server.controller.user;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.user.request.CreateAdminUserRequest;
import com.rdc.weflow_server.dto.user.request.UpdateAdminUserRequest;
import com.rdc.weflow_server.dto.user.response.AdminUserResponse;
import com.rdc.weflow_server.service.user.SystemAdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin-users")
public class SystemAdminController {

    private final SystemAdminService systemAdminService;

    /** 관리자 계정 생성 */
    @PostMapping
    public ApiResponse<AdminUserResponse> createAdmin(
            @RequestBody @Valid CreateAdminUserRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest
    ) {
        AdminUserResponse response = systemAdminService.createAdmin(
                request,
                user.getId(),
                servletRequest.getRemoteAddr()
        );

        return ApiResponse.success("SYSTEM_ADMIN_CREATED", response);
    }

    /** 관리자 계정 목록 조회 */
    @GetMapping
    public ApiResponse<List<AdminUserResponse>> getAdmins() {
        List<AdminUserResponse> admins = systemAdminService.getAdmins();
        return ApiResponse.success("SYSTEM_ADMIN_LIST", admins);
    }

    /** 상세 조회 */
    @GetMapping("/{id}")
    public ApiResponse<AdminUserResponse> getAdminById(
            @PathVariable Long id
    ) {
        return ApiResponse.success("SYSTEM_ADMIN_DETAIL",
                        systemAdminService.getAdminById(id)
        );
    }

    /** 수정 */
    @PatchMapping("/{id}")
    public ApiResponse<AdminUserResponse> updateAdmin(
            @PathVariable Long id,
            @RequestBody UpdateAdminUserRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest
    ) {
        AdminUserResponse updated = systemAdminService.updateAdmin(
                id, request, user.getId(), servletRequest.getRemoteAddr()
        );

        return ApiResponse.success("SYSTEM_ADMIN_UPDATED", updated);
    }

    /** 삭제 */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAdmin(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest
    ) {
        systemAdminService.deleteAdmin(id, user.getId(), servletRequest.getRemoteAddr());
        return ApiResponse.success("SYSTEM_ADMIN_DELETED", null);
    }
}
