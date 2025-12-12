package com.rdc.weflow_server.dto.user.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CsvUserBatchResponse {
    private Integer totalCount;           // 총 처리 건수
    private Integer successCount;         // 성공 건수
    private Integer failureCount;         // 실패 건수
    private List<UserResponse> successUsers;   // 성공한 회원 목록
    private List<CsvUserFailure> failures;     // 실패 상세 목록
}
