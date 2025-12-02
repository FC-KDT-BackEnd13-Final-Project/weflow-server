package com.rdc.weflow_server.dto.checklist.response;

import com.rdc.weflow_server.entity.checklist.Checklist;
import com.rdc.weflow_server.entity.checklist.ChecklistQuestion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class TemplateDetailResponse {

    private Long templateId;
    private String title;
    private String description;
    private String category;
    private boolean isLocked;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private List<QuestionDetailResponse> questions;

    public static TemplateDetailResponse from(Checklist template, List<ChecklistQuestion> questionList) {
        return TemplateDetailResponse.builder()
                .templateId(template.getId())
                .title(template.getTitle())
                .description(template.getDescription())
                .category(template.getCategory())
                .isLocked(template.getIsLocked())
                .createdDate(template.getCreatedAt())
                .lastModifiedDate(template.getUpdatedAt())
                .questions(questionList.stream().map(QuestionDetailResponse::from).toList())
                .build();
    }
}

