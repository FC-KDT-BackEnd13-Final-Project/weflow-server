package com.rdc.weflow_server.dto.checklist.response;

import com.rdc.weflow_server.entity.checklist.Checklist;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TemplateResponse {

    private Long templateId;
    private String title;
    private String description;
    private String category;
    private boolean isLocked;
    private int questionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TemplateResponse from(Checklist template, int questionCount) {
        return TemplateResponse.builder()
                .templateId(template.getId())
                .title(template.getTitle())
                .description(template.getDescription())
                .category(template.getCategory())
                .isLocked(template.getIsLocked())
                .questionCount(questionCount)
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}

