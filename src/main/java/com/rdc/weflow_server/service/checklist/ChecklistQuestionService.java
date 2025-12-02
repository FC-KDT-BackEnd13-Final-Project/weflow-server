package com.rdc.weflow_server.service.checklist;

import com.rdc.weflow_server.dto.checklist.request.QuestionReorderRequest;
import com.rdc.weflow_server.dto.checklist.request.QuestionRequest;
import com.rdc.weflow_server.entity.checklist.Checklist;
import com.rdc.weflow_server.entity.checklist.ChecklistQuestion;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.checklist.ChecklistOptionRepository;
import com.rdc.weflow_server.repository.checklist.ChecklistQuestionRepository;
import com.rdc.weflow_server.repository.checklist.ChecklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChecklistQuestionService {
    private final ChecklistRepository checklistRepository;
    private final ChecklistQuestionRepository questionRepository;
    private final ChecklistOptionRepository optionRepository;

    // 질문 생성
    public Long createQuestion(QuestionRequest request) {

        Checklist checklist = checklistRepository.findById(request.getChecklistId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));

        int nextOrder = questionRepository.countByChecklist(checklist) + 1;

        ChecklistQuestion question = ChecklistQuestion.builder()
                .checklist(checklist)
                .questionText(request.getQuestionText())
                .questionType(request.getQuestionType())
                .orderIndex(nextOrder)
                .build();

        ChecklistQuestion saved = questionRepository.save(question);

        return saved.getId();
    }

    // 질문 수정
    public Long updateQuestion(Long questionId, QuestionRequest request) {

        ChecklistQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_QUESTION_NOT_FOUND));

        question.updateQuestion(
                request.getQuestionText(),
                request.getQuestionType(),
                null
        );

        return question.getId();
    }

    // 질문 삭제
    public void deleteQuestion(Long questionId) {

        ChecklistQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_QUESTION_NOT_FOUND));

        questionRepository.delete(question);
    }

    // 질문 순서 재정렬
    @Transactional
    public void reorderQuestions(QuestionReorderRequest request) {

        // 체크리스트 존재 여부 확인
        checklistRepository.findById(request.getChecklistId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));

        // 새 순서 리스트
        List<Long> orderedIds = request.getOrderedIds();

        for (int i = 0; i < orderedIds.size(); i++) {
            Long questionId = orderedIds.get(i);

            ChecklistQuestion question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_QUESTION_NOT_FOUND));

            // 이 질문이 해당 체크리스트에 소속된 질문인지 검증
            if (!question.getChecklist().getId().equals(request.getChecklistId())) {
                throw new BusinessException(ErrorCode.CHECKLIST_QUESTION_NOT_IN_CHECKLIST);
            }

            // orderIndex 재부여
            question.updateQuestion(
                    question.getQuestionText(),  // 내용은 그대로
                    question.getQuestionType(),
                    i + 1                       // 새 순서
            );
        }
    }
}
