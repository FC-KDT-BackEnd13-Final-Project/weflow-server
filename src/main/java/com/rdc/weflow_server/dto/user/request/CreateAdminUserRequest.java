package com.rdc.weflow_server.dto.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateAdminUserRequest {

    @NotBlank
    private String name;

    @Email
    private String email;

    private String phoneNumber;

    @NotBlank
    private String password;
}
