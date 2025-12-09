package com.rdc.weflow_server.dto.log;

import com.rdc.weflow_server.entity.log.ActivityLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponseDto {
    private Long logId;
    private String actionType;     // CREATE, UPDATE, DELETE, LOGIN, LOGOUT, APPROVE 등
    private String targetTable;    // POST, COMMENT, PROJECT, USER ...
    private Long targetId;
    private String ipAddress;
    private LocalDateTime createdAt;
    private Long userId;
    private String userName;
    private Long projectId;
    private String projectName;
    public static ActivityLogResponseDto from(ActivityLog log) {
        return ActivityLogResponseDto.builder()
                .logId(log.getId())
                .actionType(log.getActionType() != null ? log.getActionType().name() : null)
                .targetTable(log.getTargetTable() != null ? log.getTargetTable().name() : null)
                .targetId(log.getTargetId())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .userName(log.getUser() != null ? log.getUser().getName() : null)
                .projectId(log.getProject() != null ? log.getProject().getId() : null)
                .projectName(log.getProject() != null ? log.getProject().getName() : null)
                .build();
    }
}
