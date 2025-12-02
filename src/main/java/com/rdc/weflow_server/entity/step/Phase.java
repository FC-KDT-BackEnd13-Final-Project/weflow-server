package com.rdc.weflow_server.entity.step;

// 시스템 고정 Phase 값 (사용자 생성/수정 불가)
public enum Phase { // 상태 (가장 큰 틀)

    CONTRACT,
    IN_PROGRESS,
    DELIVERY,
    MAINTENANCE,
    CLOSED
}
