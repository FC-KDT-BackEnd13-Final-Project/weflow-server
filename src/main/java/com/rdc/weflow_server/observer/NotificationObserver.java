package com.rdc.weflow_server.observer;

import com.rdc.weflow_server.entity.notification.NotificationType;
import com.rdc.weflow_server.entity.project.ProjectMember;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.event.checklist.ChecklistCreatedEvent;
import com.rdc.weflow_server.event.checklist.ChecklistSubmittedEvent;
import com.rdc.weflow_server.repository.project.ProjectMemberRepository;
import com.rdc.weflow_server.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

// 체크리스트 관련 이벤트 발생 시 프로젝트 멤버들에게 알림을 발송하는 리스너/옵저버
@Component
@RequiredArgsConstructor
public class NotificationObserver {
    private final NotificationService notificationService;
    private final ProjectMemberRepository projectMemberRepository;

    // 유틸리티 메서드: 알림 수신 대상 (프로젝트 멤버) 조회
    private List<User> getProjectTargetUsers(Long projectId) {
        return projectMemberRepository.findByProjectIdAndDeletedAtIsNull(projectId)
                .stream()
                .map(ProjectMember::getUser)
                .toList();
    }


    // 체크리스트 생성
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onChecklistCreated(ChecklistCreatedEvent event) {
        String title = event.checklistTitle();
        String message = String.format("[%s] 체크리스트가 생성되었습니다.", title);

        List<User> targetUsers = getProjectTargetUsers(event.project().getId());

        for (User user : targetUsers) {
            notificationService.send(
                    user,
                    NotificationType.CHECKLIST_CREATED,
                    "새 체크리스트 생성",
                    message,
                    event.project(),
                    null,
                    null
            );
        }
    }

    // 체크리스트 제출
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onChecklistSubmitted(ChecklistSubmittedEvent event) {
        String title = event.checklistTitle();
        String message = String.format("[%s] 체크리스트가 제출되었습니다.", title);

        List<User> targetUsers = getProjectTargetUsers(event.project().getId());

        for (User user : targetUsers) {
            notificationService.send(
                    user,
                    NotificationType.CHECKLIST_SUBMITTED,
                    "체크리스트 제출",
                    message,
                    event.project(),
                    null,
                    null
            );
        }
    }
}
