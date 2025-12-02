package com.rdc.weflow_server.controller.checklist;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.dto.checklist.request.QuestionReorderRequest;
import com.rdc.weflow_server.dto.checklist.request.QuestionRequest;
import com.rdc.weflow_server.service.checklist.ChecklistQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {
    private final ChecklistQuestionService questionService;

    // 질문 생성
    @PostMapping
    public ApiResponse<Long> createQuestion(
            @RequestBody QuestionRequest request
    ) {
        Long questionId = questionService.createQuestion(request);

        return ApiResponse.success(
                "QUESTION_CREATED",
                questionId
        );
    }

    // 질문 수정
    @PatchMapping("/{questionId}")
    public ApiResponse<Long> updateQuestion(
            @PathVariable Long questionId,
            @RequestBody QuestionRequest request
    ) {
        Long id = questionService.updateQuestion(questionId, request);

        return ApiResponse.success(
                "QUESTION_UPDATED",
                id
        );
    }

    // 질문 삭제
    @DeleteMapping("/{questionId}")
    public ApiResponse<Long> deleteQuestion(@PathVariable Long questionId) {
        questionService.deleteQuestion(questionId);

        return ApiResponse.success(
                "QUESTION_DELETED",
                questionId
        );
    }

    // 질문 순서 재정렬
    @PatchMapping("/reorder")
    public ApiResponse<Void> reorderQuestions(
            @RequestBody QuestionReorderRequest request
    ) {
        questionService.reorderQuestions(request);
        return ApiResponse.success("QUESTIONS_REORDERED", null);
    }
}
