package com.rdc.weflow_server.dto.project.request;

import lombok.Getter;

@Getter
public class ProjectRoleUpdateRequest {
    private String projectRole; // "ADMIN" 또는 "MEMBER"
}
