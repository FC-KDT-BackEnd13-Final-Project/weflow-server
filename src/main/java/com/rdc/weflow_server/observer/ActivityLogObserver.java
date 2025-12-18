package com.rdc.weflow_server.observer;

import com.rdc.weflow_server.entity.log.ActionType;
import com.rdc.weflow_server.entity.log.TargetTable;
import com.rdc.weflow_server.event.checklist.ChecklistCreatedEvent;
import com.rdc.weflow_server.event.checklist.ChecklistDeletedEvent;
import com.rdc.weflow_server.event.checklist.ChecklistSubmittedEvent;
import com.rdc.weflow_server.event.checklist.ChecklistUpdatedEvent;
import com.rdc.weflow_server.service.log.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ActivityLogObserver {
    private final ActivityLogService activityLogService;

    // 체크리스트 생성
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onChecklistCreated(ChecklistCreatedEvent event) {
        System.out.println("DEBUG: ChecklistCreatedEvent 수신 성공! ID: " + event.checklistId());
        activityLogService.createLog(
                ActionType.CREATE,
                TargetTable.CHECKLIST,
                event.checklistId(),
                event.actor().getId(),
                event.project().getId(),
                event.ipAddress()
        );
    }

    // 체크리스트 제출
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onChecklistSubmitted(ChecklistSubmittedEvent event) {
        activityLogService.createLog(
                ActionType.SUBMIT,
                TargetTable.CHECKLIST,
                event.checklistId(),
                event.actor().getId(),
                event.project().getId(),
                event.ipAddress()
        );
    }

    // 체크리스트 수정
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onChecklistUpdated(ChecklistUpdatedEvent event) {
        activityLogService.createLog(
                ActionType.UPDATE,
                TargetTable.CHECKLIST,
                event.checklistId(),
                event.actor().getId(),
                event.projectId(),
                event.ipAddress()
        );
    }

    // 체크리스트 삭제
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onChecklistDeleted(ChecklistDeletedEvent event) {
        activityLogService.createLog(
                ActionType.DELETE,
                TargetTable.CHECKLIST,
                event.checklistId(),
                event.actor().getId(),
                event.projectId(),
                event.ipAddress()
        );
    }
}
