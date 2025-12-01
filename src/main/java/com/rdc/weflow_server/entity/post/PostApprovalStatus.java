package com.rdc.weflow_server.entity.post;

public enum PostApprovalStatus {
    NORMAL, // 일반 게시글 (질문 없는 그냥 글)
    WAITING_CONFIRM, // 승인/동의 요청 상태
    CONFIRMED, // 승인됨
    REJECTED, // 거절/수정요청됨
    DELETED // 소프트 삭제
}
