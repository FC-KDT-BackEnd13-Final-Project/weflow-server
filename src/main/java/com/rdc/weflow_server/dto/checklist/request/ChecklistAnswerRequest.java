package com.rdc.weflow_server.dto.checklist.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ChecklistAnswerRequest {

    private Long checklistId;
    private List<AnswerItem> answers;

    @Getter
    @NoArgsConstructor
    public static class AnswerItem {
        private Long questionId;
        private Long optionId;      // 객관식 선택
        private String answerText;  // 주관식 or 기타입력
    }
}
