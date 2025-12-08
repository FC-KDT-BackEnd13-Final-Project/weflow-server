package com.rdc.weflow_server.dto.project.request;

import lombok.Getter;

@Getter
public class AdminProjectMemberAddRequest {
    private Long userId;
    private String projectRole;
}
