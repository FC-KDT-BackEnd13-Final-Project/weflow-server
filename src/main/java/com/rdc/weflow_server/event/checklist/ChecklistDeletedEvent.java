package com.rdc.weflow_server.event.checklist;

import com.rdc.weflow_server.entity.user.User;

/**
 * Checklist가 성공적으로 삭제되었을 때 발행되는 이벤트 레코드
 * @param source 이벤트 발생 주체 (ChecklistService 객체)
 * @param checklistId 삭제된 Checklist의 ID
 * @param projectId Checklist가 속했던 Project의 ID
 * @param actor Checklist를 삭제한 User 객체
 * @param ipAddress 삭제 시 사용자의 IP 주소
 */
public record ChecklistDeletedEvent(
        Object source,
        Long checklistId,
        Long projectId,
        User actor,
        String ipAddress
) {
}
