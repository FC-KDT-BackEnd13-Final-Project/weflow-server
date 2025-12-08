package com.rdc.weflow_server.dto.checklist.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OptionRequest {
    private Long questionId;      // 어떤 질문의 옵션인지
    private String optionText;    // 옵션 내용
    private Boolean hasInput;     // 기타 입력란 여부
    private Integer orderIndex;   // 순서
}
