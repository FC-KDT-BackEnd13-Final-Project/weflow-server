package com.rdc.weflow_server.dto.project;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminProjectMemberDto {
    private Long userId;
    private String role;
}
