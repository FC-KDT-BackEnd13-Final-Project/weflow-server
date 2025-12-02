package com.rdc.weflow_server.controller;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.dto.request.CreateUserRequest;
import com.rdc.weflow_server.dto.response.UserResponse;
import com.rdc.weflow_server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원이 성공적으로 생성되었습니다.", response));
    }
}