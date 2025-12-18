package com.rdc.weflow_server.event.checklist;

import com.rdc.weflow_server.entity.user.User;

/**
 * Checklist가 성공적으로 수정되었을 때 발행되는 이벤트 레코드
 * @param source 이벤트 발생 주체 (ChecklistService 객체)
 * @param checklistId 수정된 Checklist의 ID
 * @param projectId Checklist가 속한 Project의 ID
 * @param actor Checklist를 수정한 User 객체
 * @param ipAddress 수정 시 사용자의 IP 주소
 */
public record ChecklistUpdatedEvent(
        Object source,
        Long checklistId,
        Long projectId,
        User actor,
        String ipAddress
) {
}