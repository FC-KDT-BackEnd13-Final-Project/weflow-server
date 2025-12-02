package com.rdc.weflow_server.dto.checklist.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ChecklistCreateRequest {

    @NotNull(message = "단계 ID는 필수입니다.")
    private Long stepId;
    private Long templateId;     // 템플릿 기반 생성 시
    private String title;        // 직접 생성 시
    private String description;  // 직접 생성 시
}

