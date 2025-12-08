package com.rdc.weflow_server.dto.checklist.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class QuestionReorderRequest {
    private Long checklistId;
    private List<Long> orderedIds; // 새로운 질문 순서 ex) orderedIds = [3, 1, 5, 2]
}
