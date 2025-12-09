package com.rdc.weflow_server.repository.notification;

import com.rdc.weflow_server.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {

    // 읽지 않은 알림 개수 조회
    long countByUserIdAndIsReadFalseAndDeletedAtIsNull(Long userId);

    // 읽지 않은 알림 목록 조회 (전체 읽음 처리용)
    List<Notification> findByUserIdAndIsReadFalseAndDeletedAtIsNull(Long userId);

    // 삭제되지 않은 알림 목록 조회 (전체 삭제용)
    List<Notification> findByUserIdAndDeletedAtIsNull(Long userId);

    long countByUserIdAndIsReadFalse(Long userId);
    List<Notification> findTop5ByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);
}