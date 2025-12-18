package com.rdc.weflow_server.service.notification;

import com.rdc.weflow_server.dto.notification.request.NotificationSearchCondition;
import com.rdc.weflow_server.dto.notification.response.NotificationResponse;
import com.rdc.weflow_server.entity.notification.Notification;
import com.rdc.weflow_server.entity.notification.NotificationPriority;
import com.rdc.weflow_server.entity.notification.NotificationType;
import com.rdc.weflow_server.entity.post.Post;
import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.step.StepRequest;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService; // [1] EmailService 주입 추가

    // ========== 조회 기능 ==========

    /**
     * 1. 알림 목록 조회
     */
    public Page<NotificationResponse> getNotifications(
            Long userId,
            NotificationSearchCondition condition,
            Pageable pageable
    ) {
        Page<Notification> notifications = notificationRepository.searchNotifications(
                userId,
                condition,
                pageable
        );
        return notifications.map(NotificationResponse::from);
    }

    /**
     * 2. 읽지 않은 알림 개수 조회
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalseAndDeletedAtIsNull(userId);
    }

    /**
     * 3. 알림 상세 조회
     */
    public NotificationResponse getNotification(Long userId, Long notificationId) {
        Notification notification = validateOwner(userId, notificationId);
        return NotificationResponse.from(notification);
    }

    // ========== 상태 변경 기능 ==========

    /**
     * 4. 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = validateOwner(userId, notificationId);
        notification.markAsRead();
    }

    /**
     * 5. 알림 읽지 않음 처리
     */
    @Transactional
    public void markAsUnread(Long userId, Long notificationId) {
        Notification notification = validateOwner(userId, notificationId);
        notification.markAsUnread();
    }

    /**
     * 6. 알림 전체 읽음 처리
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository
                .findByUserIdAndIsReadFalseAndDeletedAtIsNull(userId);

        notifications.forEach(Notification::markAsRead);
    }

    /**
     * 7. 알림 삭제 (단건)
     */
    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        Notification notification = validateOwner(userId, notificationId);
        notification.softDelete();
    }

    /**
     * 8. 알림 전체 삭제
     */
    @Transactional
    public void deleteAllNotifications(Long userId) {
        List<Notification> notifications = notificationRepository
                .findByUserIdAndDeletedAtIsNull(userId);

        notifications.forEach(Notification::softDelete);
    }

    // ========== 알림 생성 (내부 호출용) ==========

    /**
     * 9. 알림 생성 및 발송
     */
    @Transactional
    public void send(
            User receiver,
            NotificationType type,
            String title,
            String message,
            Project project,
            Post post,
            StepRequest stepRequest
    ) {
        // 1. 우선순위 자동 결정
        NotificationPriority priority = determinePriority(type);

        // 2. 알림 엔티티 생성 및 저장
        Notification notification = Notification.builder()
                .user(receiver)
                .type(type)
                .priority(priority)
                .title(title)
                .message(message)
                .project(project)
                .post(post)
                .stepRequest(stepRequest)
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        // [2] 중요 알림이고, 이메일이 있고, 이메일 알림이 활성화되었다면 메일 발송
        if (priority == NotificationPriority.IMPORTANT
                && receiver.getEmail() != null
                && Boolean.TRUE.equals(receiver.getIsEmailNotificationEnabled())) {
            // 이메일 본문 생성
            String emailBody = createEmailBody(message, project, receiver.getName());

            // 이메일 전송 (비동기)
            emailService.sendEmail(receiver.getEmail(), title, emailBody);
        }
    }

    // ========== 내부 메서드 ==========

    /**
     * 이메일 본문 생성
     */
    private String createEmailBody(String message, Project project, String userName) {
        StringBuilder sb = new StringBuilder();
        sb.append("안녕하세요, ").append(userName).append("님.\n\n");
        sb.append(message).append("\n\n");

        if (project != null) {
            sb.append("관련 프로젝트: ").append(project.getName()).append("\n");
        }

        sb.append("\nWeFlow 시스템에 접속하여 확인해주세요.");
        sb.append("감사합니다.\n");
        sb.append("WeFlow 팀");
        return sb.toString();
    }

    /**
     * 알림 소유자 검증
     */
    private Notification validateOwner(Long userId, Long notificationId) {
        // 1. 알림 조회
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // 2. 삭제된 알림 체크
        if (notification.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        // 3. 권한 체크
        if (!notification.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return notification;
    }

    /**
     * 알림 타입에 따른 우선순위 자동 결정
     */
    private NotificationPriority determinePriority(NotificationType type) {
        return switch (type) {
            case STEP_REQUEST, STEP_DECISION, MENTION,
                 PASSWORD_CHANGED, PASSWORD_RESET_BY_ADMIN -> NotificationPriority.IMPORTANT;
            default -> NotificationPriority.NORMAL;
        };
    }
}