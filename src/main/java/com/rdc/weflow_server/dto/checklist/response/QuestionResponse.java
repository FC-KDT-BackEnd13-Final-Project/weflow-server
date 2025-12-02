package com.rdc.weflow_server.dto.checklist.response;

import com.rdc.weflow_server.entity.checklist.ChecklistAnswer;
import com.rdc.weflow_server.entity.checklist.ChecklistQuestion;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class QuestionResponse {
    private Long questionId;
    private String questionText;
    private String questionType;
    private Integer orderIndex;
    private List<OptionResponse> options;
    private AnswerResponse answer;

    public static QuestionResponse from(ChecklistQuestion question, ChecklistAnswer answer) {
        return QuestionResponse.builder()
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType().name())
                .orderIndex(question.getOrderIndex())
                .options(
                        question.getOptions().stream()
                                .map(OptionResponse::from)
                                .collect(Collectors.toList())
                )
                .answer(answer != null ? AnswerResponse.from(answer) : null)
                .build();
    }
}