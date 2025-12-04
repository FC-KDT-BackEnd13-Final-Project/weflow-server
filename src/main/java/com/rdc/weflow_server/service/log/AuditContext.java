package com.rdc.weflow_server.service.log;

/**
 * 로그 기록에 필요한 공통 컨텍스트를 담는 경량 DTO.
 */
public record AuditContext(
        Long userId,
        String ipAddress,
        Long projectId
) {
}
