package com.rdc.weflow_server.event.checklist;

import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.user.User;

/**
 * Checklist가 성공적으로 생성되었을 때 발행되는 이벤트 레코드
 * @param source         이벤트 발생 주체 (일반적으로 ChecklistService 객체)
 * @param checklistId    생성된 Checklist의 ID
 * @param project      Checklist가 속한 Project의
 * @param actor          Checklist를 생성한 User 객체
 * @param ipAddress      생성 시 사용자의 IP 주소
 * @param checklistTitle 알림 등에 사용될 Checklist 제목
 */
public record ChecklistCreatedEvent(
        Object source,
        Long checklistId,
        Project project,
        User actor,
        String ipAddress,
        String checklistTitle
) {
}