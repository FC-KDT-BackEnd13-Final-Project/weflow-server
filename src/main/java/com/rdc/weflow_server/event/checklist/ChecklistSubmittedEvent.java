package com.rdc.weflow_server.event.checklist;

import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.user.User;

/**
 * Checklist 답변이 성공적으로 제출(Submit)되었을 때 발행되는 이벤트 레코드
 * 제출 후 체크리스트는 자동으로 잠금(lock) 처리됩니다.
 * @param source 이벤트 발생 주체 (일반적으로 ChecklistService 객체)
 * @param checklistId 제출된 Checklist의 ID
 * @param project Checklist가 속한 Project의 ID
 * @param actor 답변을 제출한 User 객체
 * @param ipAddress 제출 시 사용자의 IP 주소
 * @param checklistTitle 알림 등에 사용될 Checklist 제목
 */
public record ChecklistSubmittedEvent(
        Object source,
        Long checklistId,
        Project project,
        User actor,
        String ipAddress,
        String checklistTitle
) {
}