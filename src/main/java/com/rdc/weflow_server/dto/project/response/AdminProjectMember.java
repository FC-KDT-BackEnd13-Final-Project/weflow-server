package com.rdc.weflow_server.dto.project.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminProjectMember {
    private Long userId;
    private String role;
}
