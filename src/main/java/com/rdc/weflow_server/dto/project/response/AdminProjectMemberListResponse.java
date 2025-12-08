package com.rdc.weflow_server.dto.project.response;

import com.rdc.weflow_server.entity.project.ProjectMember;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminProjectMemberListResponse {
    private long totalCount;
    private List<AdminProjectMemberListItem> members;

    public static AdminProjectMemberListResponse of(List<ProjectMember> members) {
        return AdminProjectMemberListResponse.builder()
                .totalCount(members.size())
                .members(
                        members.stream()
                                .map(AdminProjectMemberListItem::from)
                                .toList()
                )
                .build();
    }
}
