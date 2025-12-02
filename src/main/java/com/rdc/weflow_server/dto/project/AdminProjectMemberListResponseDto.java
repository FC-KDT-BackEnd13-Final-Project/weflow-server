package com.rdc.weflow_server.dto.project;

import com.rdc.weflow_server.entity.project.ProjectMember;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminProjectMemberListResponseDto {
    private long totalCount;
    private List<AdminProjectMemberListItemDto> members;

    public static AdminProjectMemberListResponseDto of(List<ProjectMember> members) {
        return AdminProjectMemberListResponseDto.builder()
                .totalCount(members.size())
                .members(
                        members.stream()
                                .map(AdminProjectMemberListItemDto::from)
                                .toList()
                )
                .build();
    }
}
