package com.rdc.weflow_server.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CsvUserFailure {
    private Integer rowNumber;   // CSV 행 번호 (헤더 제외, 1부터)
    private String email;        // 실패한 이메일
    private String name;         // 실패한 이름
    private String reason;       // 실패 사유
}
