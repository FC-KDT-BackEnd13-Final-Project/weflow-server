package com.rdc.weflow_server.dto.checklist.response;

import com.rdc.weflow_server.entity.checklist.ChecklistAnswer;
import com.rdc.weflow_server.entity.checklist.ChecklistQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponse {
    private Long questionId;
    private String questionText;
    private String questionType;
    private Integer orderIndex;
    private List<OptionResponse> options;
    private AnswerResponse answer;

    public QuestionResponse(ChecklistQuestion q, AnswerResponse answer) {
        this.questionId = q.getId();
        this.questionText = q.getQuestionText();
        this.questionType = q.getQuestionType().name();
        this.orderIndex = q.getOrderIndex();
        this.options = q.getOptions().stream()
                .map(OptionResponse::from)
                .toList();
        this.answer = answer;
    }

    public static QuestionResponse from(ChecklistQuestion q, List<ChecklistAnswer> answers) {

        AnswerResponse answerResponse = null;

        switch (q.getQuestionType()) {
            case TEXT -> {
                ChecklistAnswer textAnswer = answers.isEmpty() ? null : answers.get(0);
                answerResponse = AnswerResponse.fromText(textAnswer);
            }
            case SINGLE -> {
                ChecklistAnswer singleAnswer = answers.isEmpty() ? null : answers.get(0);
                answerResponse = AnswerResponse.fromSingle(singleAnswer);
            }
            case MULTI -> {
                answerResponse = AnswerResponse.fromMulti(answers);
            }
        }

        return new QuestionResponse(q, answerResponse);
    }

}