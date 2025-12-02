package com.rdc.weflow_server.dto.checklist.response;

import com.rdc.weflow_server.entity.checklist.ChecklistQuestion;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class QuestionDetailResponse {

    private Long questionId;
    private String questionText;
    private String questionType;
    private Integer orderIndex;

    private List<OptionResponse> options;   // 옵션 리스트

    public static QuestionDetailResponse from(ChecklistQuestion question) {
        return QuestionDetailResponse.builder()
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType().name())
                .orderIndex(question.getOrderIndex())
                .options(question.getOptions().stream()
                        .map(OptionResponse::from)
                        .toList()
                )
                .build();
    }
}

