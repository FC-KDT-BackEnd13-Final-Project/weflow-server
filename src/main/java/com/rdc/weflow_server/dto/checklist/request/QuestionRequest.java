package com.rdc.weflow_server.dto.checklist.request;

import com.rdc.weflow_server.entity.checklist.ChecklistQuestion.QuestionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuestionRequest {
    private Long checklistId;
    private String questionText;
    private QuestionType questionType;
}
