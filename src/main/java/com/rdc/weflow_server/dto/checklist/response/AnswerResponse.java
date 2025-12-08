package com.rdc.weflow_server.dto.checklist.response;

import com.rdc.weflow_server.entity.checklist.ChecklistAnswer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AnswerResponse {

    private Long selectedOptionId;        // SINGLE
    private List<Long> selectedOptionIds; // MULTI
    private String answerText;            // TEXT 또는 객관식-기타 입력내용

    public static AnswerResponse fromSingle(ChecklistAnswer answer) {
        if (answer == null) {
            return null;
        }

        return AnswerResponse.builder()
                .selectedOptionId(
                        answer.getSelectedOption() != null
                                ? answer.getSelectedOption().getId()
                                : null
                )
                .selectedOptionIds(null)
                .answerText(answer.getAnswerText())
                .build();
    }

    public static AnswerResponse fromMulti(List<ChecklistAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            return null;
        }

        List<Long> optionIds = answers.stream()
                .map(a -> a.getSelectedOption().getId())
                .toList();

        // MULTI에서 answerText는 여러 개가 아니라 “하나만 존재하거나 null”
        String text = answers.stream()
                .map(ChecklistAnswer::getAnswerText)
                .filter(t -> t != null)
                .findFirst()
                .orElse(null);

        return AnswerResponse.builder()
                .selectedOptionId(null)
                .selectedOptionIds(optionIds)
                .answerText(text)
                .build();
    }

    public static AnswerResponse fromText(ChecklistAnswer answer) {
        if (answer == null) {
            return null;
        }

        return AnswerResponse.builder()
                .selectedOptionId(null)
                .selectedOptionIds(null)
                .answerText(answer.getAnswerText())
                .build();
    }
}