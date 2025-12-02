package com.rdc.weflow_server.dto.checklist.response;

import com.rdc.weflow_server.entity.checklist.ChecklistAnswer;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnswerResponse {
    private Long selectedOptionId;
    private String answerText;

    public static AnswerResponse from(ChecklistAnswer answer) {
        if (answer == null) return null;

        return AnswerResponse.builder()
                .selectedOptionId(
                        answer.getSelectedOption() != null
                                ? answer.getSelectedOption().getId()
                                : null
                )
                .answerText(answer.getAnswerText())
                .build();
    }
}

