package com.rdc.weflow_server.service.checklist;

import com.rdc.weflow_server.dto.checklist.request.ChecklistAnswerRequest;
import com.rdc.weflow_server.dto.checklist.request.ChecklistCreateRequest;
import com.rdc.weflow_server.dto.checklist.request.ChecklistUpdateRequest;
import com.rdc.weflow_server.dto.checklist.response.ChecklistDetailResponse;
import com.rdc.weflow_server.dto.checklist.response.ChecklistResponse;
import com.rdc.weflow_server.dto.checklist.response.QuestionResponse;
import com.rdc.weflow_server.entity.checklist.Checklist;
import com.rdc.weflow_server.entity.checklist.ChecklistAnswer;
import com.rdc.weflow_server.entity.checklist.ChecklistOption;
import com.rdc.weflow_server.entity.checklist.ChecklistQuestion;
import com.rdc.weflow_server.entity.step.Step;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.checklist.ChecklistAnswerRepository;
import com.rdc.weflow_server.repository.checklist.ChecklistOptionRepository;
import com.rdc.weflow_server.repository.checklist.ChecklistQuestionRepository;
import com.rdc.weflow_server.repository.checklist.ChecklistRepository;
import com.rdc.weflow_server.repository.step.StepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChecklistService {
    private final ChecklistRepository checklistRepository;
    private final ChecklistQuestionRepository questionRepository;
    private final ChecklistOptionRepository optionRepository;
    private final StepRepository stepRepository;
    private final ChecklistAnswerRepository answerRepository;

    // 체크리스트 생성
    public Long createChecklist(ChecklistCreateRequest request) {

        // Step 가져오기
        Step step = stepRepository.findById(request.getStepId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STEP_NOT_FOUND));

        // 체크리스트 생성
        Checklist checklist = Checklist.builder()
                .title(request.getTitle() != null ? request.getTitle() : step.getTitle() + " 체크리스트")
                .description(request.getDescription())
                .isTemplate(false)
                .isLocked(false)
                .step(step)
                .build();

        checklistRepository.save(checklist);

        // 템플릿 기반 복사
        if (request.getTemplateId() != null) {
            Checklist template = checklistRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));

            copyTemplate(template, checklist);
        }

        return checklist.getId();
    }

    // 프로젝트별 체크리스트 목록 조회
    @Transactional(readOnly = true)
    public List<ChecklistResponse> getProjectChecklists(Long projectId) {

        List<Checklist> checklists =
                checklistRepository.findByStep_Project_IdOrderByCreatedAtDesc(projectId);

        return checklists.stream()
                .map(ChecklistResponse::from)
                .toList();
    }

    // 체크리스트 상세 조회
    @Transactional(readOnly = true)
    public ChecklistDetailResponse getChecklistDetail(Long checklistId) {

        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));

        // 해당 체크리스트의 모든 답변 조회
        List<ChecklistAnswer> answers = answerRepository.findByChecklist_Id(checklistId);

        // questionId → answer 매핑 (빠른 접근 위한 Map)
        Map<Long, ChecklistAnswer> answerMap = answers.stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a));

        List<QuestionResponse> questionDtos =
                checklist.getQuestions().stream()
                        .map(q -> {
                            ChecklistAnswer answer = answerMap.get(q.getId());
                            return QuestionResponse.from(q, answer);
                        })
                        .toList();
        return ChecklistDetailResponse.from(checklist, questionDtos);
    }

    // 체크리스트 수정
    public void updateChecklist(Long checklistId, ChecklistUpdateRequest request) {
        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));

        if (checklist.getIsLocked()) {
            throw new BusinessException(ErrorCode.CHECKLIST_LOCKED);
        }

        // 제목/설명 먼저 수정
        checklist.updateChecklist(
                request.getTitle(),
                request.getDescription()
        );

        // 단계 변경 요청이 있으면 처리
        if (request.getStepId() != null) {

            Step newStep = stepRepository.findById(request.getStepId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.STEP_NOT_FOUND));

            // 다른 프로젝트 단계로는 이동 불가
//            if (!newStep.getProject().getId().equals(
//                    checklist.getStep().getProject().getId())) {
//                throw new BusinessException(ErrorCode.INVALID_STEP_PROJECT);
//            }

            // 단계 업데이트
            checklist.changeStep(newStep);
        }
    }

    // 체크리스트 삭제
    public void deleteChecklist(Long checklistId) {
        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));

        // 잠긴 체크리스트는 삭제 불가
        if (checklist.getIsLocked()) {
            throw new BusinessException(ErrorCode.CHECKLIST_LOCKED);
        }

        checklistRepository.delete(checklist);
    }

    // 체크리스트 답변 제출
    public void submitChecklistAnswers(ChecklistAnswerRequest request, User user) {

        Checklist checklist = checklistRepository.findById(request.getChecklistId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));

        // 이미 잠겨 있으면 제출 불가
        if (checklist.getIsLocked()) {
            throw new BusinessException(ErrorCode.CHECKLIST_LOCKED);
        }

        for (ChecklistAnswerRequest.AnswerItem item : request.getAnswers()) {

            // 1) 질문 조회
            ChecklistQuestion question = questionRepository.findById(item.getQuestionId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_QUESTION_NOT_FOUND));

            ChecklistOption option = null;

            // 2) optionId 있을 경우 옵션 조회
            if (item.getOptionId() != null) {
                option = optionRepository.findById(item.getOptionId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_OPTION_NOT_FOUND));

                // 옵션이 해당 질문 소속인지 검증
                if (!option.getQuestion().getId().equals(question.getId())) {
                    throw new BusinessException(ErrorCode.CHECKLIST_OPTION_NOT_IN_QUESTION);
                }
            }

            // 3) 객관식-기타(hasInput=true)인데 텍스트 없으면 오류
            if (option != null && option.getHasInput() && item.getAnswerText() == null) {
                throw new BusinessException(ErrorCode.REQUIRED_ANSWER_INPUT);
            }

            // 4) 선택형인데 hasInput=false 인데 answerText가 들어오면 오류
            if (option != null && !option.getHasInput() && item.getAnswerText() != null) {
                throw new BusinessException(ErrorCode.INVALID_ANSWER_INPUT);
            }

            // 5) 답변 저장
            ChecklistAnswer answer = ChecklistAnswer.builder()
                    .checklist(checklist)
                    .question(question)
                    .selectedOption(option)
                    .answerText(item.getAnswerText())
                    .answeredBy(user)
                    .answeredAt(LocalDateTime.now())
                    .build();

            answerRepository.save(answer);
        }

        // 6) 답변 제출 후 체크리스트 잠금
        checklist.lockChecklist();
    }

    // 템플릿 복사
    private void copyTemplate(Checklist template, Checklist newChecklist) {
        // 부모 템플릿 연결
        newChecklist.linkTemplate(template);

        for (ChecklistQuestion q : template.getQuestions()) {

            ChecklistQuestion copiedQ = ChecklistQuestion.builder()
                    .checklist(newChecklist)
                    .questionText(q.getQuestionText())
                    .questionType(q.getQuestionType())
                    .orderIndex(q.getOrderIndex())
                    .build();

            questionRepository.save(copiedQ);

            for (ChecklistOption o : q.getOptions()) {

                ChecklistOption copiedO = ChecklistOption.builder()
                        .question(copiedQ)
                        .optionText(o.getOptionText())
                        .hasInput(o.getHasInput())
                        .orderIndex(o.getOrderIndex())
                        .build();

                optionRepository.save(copiedO);
            }
        }
    }
}
