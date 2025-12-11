package com.rdc.weflow_server.controller.user;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.user.request.CreateUserRequest;
import com.rdc.weflow_server.dto.user.request.ResetPasswordAdminRequest;
import com.rdc.weflow_server.dto.user.request.UpdateUserAdminRequest;
import com.rdc.weflow_server.dto.user.request.UserSearchCondition;
import com.rdc.weflow_server.dto.user.response.UserResponse;
import com.rdc.weflow_server.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    /**
     * 회원 생성 (관리자 전용)
     * POST /api/admin/users
     */
    @PostMapping
    public ApiResponse<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest) {

        UserResponse response = userService.createUser(
                request,
                user.getId(),
                servletRequest.getRemoteAddr()
        );
        return ApiResponse.success("회원이 성공적으로 생성되었습니다.", response);
    }

    /**
     * 회원 일괄 생성
     * POST /api/admin/users/batch
     */
    @PostMapping("/batch")
    public ApiResponse<List<UserResponse>> createUsersBatch(
            @RequestBody @Valid List<CreateUserRequest> requests,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest) {

        List<UserResponse> response = userService.createUsersBatch(
                requests,
                user.getId(),
                servletRequest.getRemoteAddr()
        );
        return ApiResponse.success("회원 일괄 생성 성공", response);
    }

    /**
     * 회원 목록 조회 (검색 + 페이징)
     * GET /api/admin/users
     * 파라미터: keyword, role, companyId, page, size
     */
    @GetMapping
    public ApiResponse<Page<UserResponse>> getUsers(
            @ModelAttribute UserSearchCondition condition,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<UserResponse> response = userService.getUsers(condition, pageable);
        return ApiResponse.success("회원 목록 조회 성공", response);
    }

    /**
     * 회원 정보 수정
     * PATCH /api/admin/users/{userId}
     */
    @PatchMapping("/{userId}")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable Long userId,
            @RequestBody @Valid UpdateUserAdminRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest) {

        UserResponse response = userService.updateUser(
                userId,
                request,
                user.getId(),
                servletRequest.getRemoteAddr()
        );
        return ApiResponse.success("회원 정보 수정 성공", response);
    }

    /**
     * 회원 삭제 (Soft Delete)
     * DELETE /api/admin/users/{userId}
     */
    @DeleteMapping("/{userId}")
    public ApiResponse<Void> deleteUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest) {

        userService.deleteUser(
                userId,
                user.getId(),
                servletRequest.getRemoteAddr()
        );
        return ApiResponse.success("회원 삭제 성공", null);
    }

    /**
     * 회원 복구
     * PATCH /api/admin/users/{userId}/restore
     */
    @PatchMapping("/{userId}/restore")
    public ApiResponse<UserResponse> restoreUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest) {

        UserResponse response = userService.restoreUser(
                userId,
                user.getId(),
                servletRequest.getRemoteAddr()
        );
        return ApiResponse.success("회원 복구 성공", response);
    }

    /**
     * 회원 비밀번호 강제 재설정 (관리자 전용)
     * PATCH /api/admin/users/{userId}/reset-password
     */
    @PatchMapping("/{userId}/reset-password")
    public ApiResponse<UserResponse> resetPassword(
            @PathVariable Long userId,
            @RequestBody @Valid ResetPasswordAdminRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest) {

        UserResponse response = userService.resetPasswordByAdmin(
                userId,
                request.getNewPassword(),
                user.getId(),
                servletRequest.getRemoteAddr()
        );
        return ApiResponse.success("비밀번호가 재설정되었습니다. 회원은 다음 로그인 시 비밀번호 변경이 필요합니다.", response);
    }
}