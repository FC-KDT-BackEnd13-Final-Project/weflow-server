package com.rdc.weflow_server.dto.checklist.response;

import com.rdc.weflow_server.entity.checklist.Checklist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ChecklistResponse {
    private Long checklistId;
    private Long stepId;
    private String stepName;

    private String title;
    private boolean isLocked;

    private int questionCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ChecklistResponse from(Checklist c) {
        return ChecklistResponse.builder()
                .checklistId(c.getId())
                .stepId(c.getStep().getId())
                .stepName(c.getStep().getTitle())
                .title(c.getTitle())
                .isLocked(c.getIsLocked())
                .questionCount(c.getQuestions().size())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
