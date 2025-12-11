package com.rdc.weflow_server.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류가 발생했습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_003", "허용되지 않은 HTTP 메서드입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_004", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_005", "권한이 없습니다."),
    FORBIDDEN_PROJECT_ACCESS(HttpStatus.FORBIDDEN, "COMMON_006","프로젝트에 접근할 권한이 없습니다."),
    PERMISSION_REQUEST_REQUIRED(HttpStatus.FORBIDDEN, "ACCESS_REQUEST_REQUIRED", "관리자 승인 요청이 필요합니다."),

    // Post
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_001", "게시글을 찾을 수 없습니다."),
    POST_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "POST_002", "이미 삭제된 게시글입니다."),
    POST_ALREADY_CLOSED(HttpStatus.BAD_REQUEST, "POST_003", "이미 완료된 게시글입니다."),
    INVALID_POST_STATUS(HttpStatus.BAD_REQUEST, "POST_004", "게시글 상태가 올바르지 않습니다."),
    POST_CANNOT_EDIT(HttpStatus.FORBIDDEN, "POST_005", "상대방이 게시글에 참여하여 수정할 수 없습니다."),

    // Post Question
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "QUESTION_001", "질문을 찾을 수 없습니다."),

    // Post Answer
    ANSWER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "ANSWER_001", "이미 답변이 등록되었습니다."),

    // Company
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMPANY_001", "회사를 찾을 수 없습니다."),
    COMPANY_BUSINESS_NUMBER_DUPLICATE(HttpStatus.CONFLICT, "COMPANY_002", "이미 등록된 사업자번호입니다."),
    COMPANY_EMAIL_DUPLICATE(HttpStatus.CONFLICT, "COMPANY_003", "이미 등록된 이메일입니다."),
    COMPANY_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "COMPANY_004", "이미 삭제된 회사입니다."),
    COMPANY_NOT_DELETED(HttpStatus.BAD_REQUEST, "COMPANY_005", "삭제되지 않은 회사입니다."),
    COMPANY_TYPE_NOT_SET(HttpStatus.BAD_REQUEST, "COMPANY_006", "회사 유형이 설정되지 않았습니다. 회사 정보를 수정하여 유형을 설정해주세요."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "회원을 찾을 수 없습니다."),
    USER_EMAIL_DUPLICATE(HttpStatus.CONFLICT, "USER_002", "이미 등록된 이메일입니다."),
    USER_PHONE_DUPLICATE(HttpStatus.CONFLICT, "USER_003", "이미 등록된 전화번호입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "USER_004", "현재 비밀번호가 일치하지 않습니다."),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "USER_005", "새 비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    USER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "USER_006", "이미 삭제된 회원입니다."),
    SYSTEM_ADMIN_MINIMUM_REQUIRED(HttpStatus.FORBIDDEN, "USER_007", "최소 1명 이상의 시스템 관리자가 필요합니다."),
    USER_NOT_DELETED(HttpStatus.BAD_REQUEST, "USER_008", "삭제되지 않은 회원입니다."),
    USER_ROLE_COMPANY_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "USER_009", "사용자 역할이 회사 유형과 일치하지 않습니다. AGENCY 회사는 AGENCY 역할만, CLIENT 회사는 CLIENT 역할만 가능합니다."),

    // Auth
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_001", "이메일 또는 비밀번호가 일치하지 않습니다."),
    USER_SUSPENDED(HttpStatus.FORBIDDEN, "AUTH_002", "정지된 계정입니다."),
    USER_DELETED(HttpStatus.FORBIDDEN, "AUTH_003", "삭제된 계정입니다."),

    // Comment
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_001", "댓글을 찾을 수 없습니다."),
    COMMENT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "COMMENT_002", "이미 삭제된 댓글입니다."),

    // Step
    STEP_NOT_FOUND(HttpStatus.NOT_FOUND, "STEP_001", "단계를 찾을 수 없습니다."),
    STEP_ALREADY_EXISTS(HttpStatus.CONFLICT, "STEP_002", "이미 존재하는 단계입니다."),
    STEP_ORDER_INVALID(HttpStatus.BAD_REQUEST, "STEP_003", "단계 순서 값이 잘못되었습니다."),
    STEP_STATUS_INVALID(HttpStatus.BAD_REQUEST, "STEP_004", "단계 상태가 올바르지 않습니다."),
    PREVIOUS_STEP_NOT_APPROVED(HttpStatus.BAD_REQUEST, "STEP_005", "이전 단계가 승인되지 않아 승인요청을 생성할 수 없습니다."),
    INVALID_STEP_ORDER(HttpStatus.BAD_REQUEST, "STEP_006", "단계 순서 정보가 올바르지 않습니다."),

    // StepRequest
    STEP_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "STEP_REQUEST_001", "승인 요청을 찾을 수 없습니다."),
    STEP_REQUEST_ALREADY_DECIDED(HttpStatus.BAD_REQUEST, "STEP_REQUEST_002", "이미 승인/반려된 요청입니다."),
    STEP_REQUEST_NOT_ALLOWED(HttpStatus.FORBIDDEN, "STEP_REQUEST_003", "이 승인요청을 처리할 권한이 없습니다."),
    STEP_REQUEST_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "STEP_REQUEST_004", "이 승인요청은 취소할 수 없습니다."),

    // Answer
    STEP_ANSWER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "STEP_ANSWER_001", "이미 승인/반려 답변이 등록되었습니다."),

    // Notification
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_001", "알림을 찾을 수 없습니다."),

    // Project
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_001", "프로젝트를 찾을 수 없습니다."),
    NO_PROJECT_PERMISSION(HttpStatus.FORBIDDEN, "PROJECT_002", "해당 프로젝트에 접근할 수 없습니다."),
    PROJECT_CREATE_FORBIDDEN(HttpStatus.FORBIDDEN, "PROJECT_003", "프로젝트 생성 권한이 없습니다."),
    PROJECT_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "PROJECT_004", "프로젝트 수정 권한이 없습니다."),
    PROJECT_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "PROJECT_005", "프로젝트 삭제 권한이 없습니다."),
    PROJECT_INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "PROJECT_006", "프로젝트 날짜 범위가 올바르지 않습니다."),
    PROJECT_COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_007", "프로젝트의 회사 정보를 찾을 수 없습니다."),

    // Project Member
    PROJECT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_MEMBER_001", "프로젝트 멤버를 찾을 수 없습니다."),
    PROJECT_MEMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "PROJECT_MEMBER_002", "이미 프로젝트에 속한 사용자입니다."),
    PROJECT_MEMBER_ADD_FORBIDDEN(HttpStatus.FORBIDDEN, "PROJECT_MEMBER_003", "프로젝트 멤버 추가 권한이 없습니다."),
    PROJECT_MEMBER_REMOVE_FORBIDDEN(HttpStatus.FORBIDDEN, "PROJECT_MEMBER_004", "프로젝트 멤버 삭제 권한이 없습니다."),
    PROJECT_MEMBER_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_MEMBER_005", "프로젝트에 추가할 사용자를 찾을 수 없습니다."),
    PROJECT_MEMBER_ROLE_INVALID(HttpStatus.BAD_REQUEST, "PROJECT_MEMBER_006", "유효하지 않은 프로젝트 역할입니다."),
    PROJECT_MEMBER_ALREADY_REMOVED(HttpStatus.BAD_REQUEST, "PROJECT_MEMBER_007", "이미 삭제된 멤버입니다."),
    CANNOT_DOWNGRADE_SELF(HttpStatus.FORBIDDEN, "PROJECT_MEMBER_008", "자기 자신을 MEMBER로 강등할 수 없습니다."),
    INVALID_PROJECT_ROLE(HttpStatus.BAD_REQUEST, "PROJECT_MEMBER_009", "유효하지 않은 프로젝트 역할 값입니다."),
    PROJECT_MEMBER_ROLE_FORBIDDEN(HttpStatus.FORBIDDEN, "PROJECT_MEMBER_010", "프로젝트 역할을 변경할 권한이 없습니다."),
    CANNOT_REMOVE_SELF(HttpStatus.FORBIDDEN, "PROJECT_MEMBER_011", "자기 자신을 삭제할 수 없습니다."),

    // Checklist
    CHECKLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "CHECKLIST_001", "체크리스트를 찾을 수 없습니다."),
    CHECKLIST_LOCKED(HttpStatus.BAD_REQUEST, "CHECKLIST_002", "잠금 처리된 체크리스트는 수정할 수 없습니다."),

    // Checklist Question
    CHECKLIST_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "CHECKLIST_QUESTION_001", "체크리스트 질문을 찾을 수 없습니다."),
    CHECKLIST_QUESTION_NOT_IN_CHECKLIST(HttpStatus.BAD_REQUEST, "CHECKLIST_QUESTION_002", "체크리스트에 속하지 않은 질문입니다."),

    // Checklist Option
    CHECKLIST_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "CHECKLIST_OPTION_001", "체크리스트 옵션을 찾을 수 없습니다."),
    CHECKLIST_OPTION_NOT_IN_QUESTION(HttpStatus.BAD_REQUEST, "CHECKLIST_OPTION_002", "질문에 속하지 않은 옵션입니다."),
    CHECKLIST_OPTION_TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "CHECKLIST_OPTION_003", "해당 질문 타입에는 옵션을 추가할 수 없습니다."),
    CHECKLIST_INVALID_OPTION_SEQUENCE(HttpStatus.BAD_REQUEST, "CHECKLIST_OPTION_004", "옵션 순서가 올바르지 않습니다."),

    // Checklist Answer
    REQUIRED_ANSWER_INPUT(HttpStatus.BAD_REQUEST, "CHECKLIST_ANSWER_001", "해당 선택지는 추가 입력이 필요합니다."),
    INVALID_ANSWER_INPUT(HttpStatus.BAD_REQUEST, "CHECKLIST_ANSWER_002", "해당 선택지에는 추가 입력을 허용하지 않습니다."),

    // Attachment
    ATTACHMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "ATTACHMENT_001", "첨부파일을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
