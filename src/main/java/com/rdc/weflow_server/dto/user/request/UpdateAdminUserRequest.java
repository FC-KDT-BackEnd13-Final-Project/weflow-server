package com.rdc.weflow_server.dto.user.request;

import lombok.Getter;

@Getter
public class UpdateAdminUserRequest {

    private String name;
    private String phoneNumber;
    private String password; // optional (변경 시 암호화)
}
