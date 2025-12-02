package com.rdc.weflow_server.dto.project;

import lombok.Getter;

@Getter
public class AdminProjectMemberAddRequestDto {
    private Long userId;
    private String projectRole;
}
