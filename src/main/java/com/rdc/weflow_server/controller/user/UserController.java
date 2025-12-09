package com.rdc.weflow_server.controller.user;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.user.request.ChangePasswordRequest;
import com.rdc.weflow_server.dto.user.request.UpdateUserRequest;
import com.rdc.weflow_server.dto.user.response.UserResponse;
import com.rdc.weflow_server.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * 내 정보 조회
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 1. Service 호출 (토큰에서 꺼낸 ID 사용)
        UserResponse response = userService.getMyInfo(userDetails.getId());

        // 2. 응답 반환
        return ResponseEntity.ok(
                ApiResponse.success("내 정보 조회 성공", response)
        );
    }

    /**
     * 내 정보 수정
     * PATCH /api/users/me
     */
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateUserRequest request,
            HttpServletRequest servletRequest
    ) {
        // 1. Service 호출 (ID와 수정할 데이터, IP 전달)
        UserResponse response = userService.updateMyInfo(
                userDetails.getId(),
                request,
                servletRequest.getRemoteAddr()
        );

        // 2. 응답 반환
        return ResponseEntity.ok(
                ApiResponse.success("내 정보 수정 성공", response)
        );
    }

    /**
     * 비밀번호 변경
     * PATCH /api/users/me/password
     */
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ChangePasswordRequest request,
            HttpServletRequest servletRequest
    ) {
        // 1. Service 호출 (IP 전달)
        userService.changePassword(
                userDetails.getId(),
                request,
                servletRequest.getRemoteAddr()
        );

        // 2. 응답 반환 (성공 시 데이터 없음)
        return ResponseEntity.ok(
                ApiResponse.success("비밀번호 변경 성공", null)
        );
    }
}