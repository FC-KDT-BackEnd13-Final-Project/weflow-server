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
import com.rdc.weflow_server.entity.log.ActionType;
import com.rdc.weflow_server.entity.log.TargetTable;
import com.rdc.weflow_server.entity.notification.NotificationType;
import com.rdc.weflow_server.entity.project.ProjectMember;
import com.rdc.weflow_server.entity.step.Step;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.event.checklist.ChecklistCreatedEvent;
import com.rdc.weflow_server.event.checklist.ChecklistDeletedEvent;
import com.rdc.weflow_server.event.checklist.ChecklistSubmittedEvent;
import com.rdc.weflow_server.event.checklist.ChecklistUpdatedEvent;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.checklist.ChecklistAnswerRepository;
import com.rdc.weflow_server.repository.checklist.ChecklistOptionRepository;
import com.rdc.weflow_server.repository.checklist.ChecklistQuestionRepository;
import com.rdc.weflow_server.repository.checklist.ChecklistRepository;
import com.rdc.weflow_server.repository.project.ProjectMemberRepository;
import com.rdc.weflow_server.repository.step.StepRepository;
import com.rdc.weflow_server.service.log.ActivityLogService;
import com.rdc.weflow_server.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    private final ApplicationEventPublisher eventPublisher;

    // 체크리스트 생성
    public Long createChecklist(ChecklistCreateRequest request, User user, String ip) {
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
                .createdBy(user)
                .build();
        checklistRepository.save(checklist);

        // 질문 저장
        for (ChecklistCreateRequest.QuestionCreateRequest qReq : request.getQuestions()) {
            ChecklistQuestion question = ChecklistQuestion.builder()
                    .questionText(qReq.getQuestionText())
                    .questionType(ChecklistQuestion.QuestionType.valueOf(qReq.getQuestionType()))
                    .orderIndex(qReq.getOrderIndex())
                    .checklist(checklist)
                    .build();

            questionRepository.save(question);

            // TEXT 타입은 options 없음
            if (qReq.getOptions() != null) {
                for (ChecklistCreateRequest.OptionCreateRequest optReq : qReq.getOptions()) {

                    ChecklistOption option = ChecklistOption.builder()
                            .optionText(optReq.getOptionText())
                            .orderIndex(optReq.getOrderIndex())
                            .hasInput(optReq.getHasInput())
                            .question(question)
                            .build();

                    optionRepository.save(option);
                }
            }
        }

        // 이벤트 발행
        eventPublisher.publishEvent(new ChecklistCreatedEvent(
                this,
                checklist.getId(),
                step.getProject(),
                user,
                ip,
                checklist.getTitle()
        ));

        return checklist.getId();
    }

    // 프로젝트별 체크리스트 목록 조회
    @Transactional(readOnly = true)
    public Page<ChecklistResponse> getProjectChecklists(Long projectId, Pageable pageable) {
        Page<Checklist> pageResult =
                checklistRepository.findByStep_Project_IdOrderByCreatedAtDesc(projectId, pageable);

        return pageResult.map(ChecklistResponse::from);
    }

    // 체크리스트 상세 조회
    @Transactional(readOnly = true)
    public ChecklistDetailResponse getChecklistDetail(Long checklistId) {

        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));

        // 해당 체크리스트의 모든 답변 조회
        List<ChecklistAnswer> answers = answerRepository.findByChecklist_Id(checklistId);

        // questionId → List<ChecklistAnswer> 매핑
        Map<Long, List<ChecklistAnswer>> answerMap = answers.stream()
                .collect(Collectors.groupingBy(a -> a.getQuestion().getId()));

        // questions + MULTI 대응 answer 묶어서 DTO 생성
        List<QuestionResponse> questionDtos =
                checklist.getQuestions().stream()
                        .map(q -> {
                            List<ChecklistAnswer> answerList = answerMap.getOrDefault(q.getId(), List.of());
                            return QuestionResponse.from(q, answerList);
                        })
                        .toList();

        return ChecklistDetailResponse.from(checklist, questionDtos);
    }


    // 체크리스트 수정
    public Long updateChecklist(Long checklistId, ChecklistUpdateRequest request, User user, String ip) {
        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));

        if (checklist.getIsLocked()) {
            throw new BusinessException(ErrorCode.CHECKLIST_LOCKED);
        }

        boolean isOwner = checklist.getCreatedBy().getId().equals(user.getId());
        boolean isSystemAdmin = user.getRole().name().equals("SYSTEM_ADMIN");

        if (!isOwner && !isSystemAdmin) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
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


            // 단계 업데이트
            checklist.changeStep(newStep);
        }

        Long projectId = checklist.getStep().getProject().getId();

        // 이벤트 발행 - 로그
        eventPublisher.publishEvent(new ChecklistUpdatedEvent(
                this,
                checklist.getId(),
                projectId,
                user,
                ip
        ));

        return checklist.getId();
    }

    // 체크리스트 삭제
    public Long deleteChecklist(Long checklistId, User user, String ip) {
        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));

        // 잠긴 체크리스트는 삭제 불가
        if (checklist.getIsLocked()) {
            throw new BusinessException(ErrorCode.CHECKLIST_LOCKED);
        }

        boolean isOwner = checklist.getCreatedBy().getId().equals(user.getId());
        boolean isSystemAdmin = user.getRole().name().equals("SYSTEM_ADMIN");

        if (!isOwner && !isSystemAdmin) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Long projectId = checklist.getStep().getProject().getId();

        checklistRepository.delete(checklist);

        // 이벤트 발행 - 로그
        eventPublisher.publishEvent(new ChecklistDeletedEvent(
                this,
                checklistId,
                projectId,
                user,
                ip
        ));
        return checklistId;
    }

    // 체크리스트 답변 제출
    public Long submitChecklistAnswers(ChecklistAnswerRequest request, User user, String ip) {

        Checklist checklist = checklistRepository.findById(request.getChecklistId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));

        if (checklist.getIsLocked()) {
            throw new BusinessException(ErrorCode.CHECKLIST_LOCKED);
        }

        // questionId 기준 그룹화
        Map<Long, List<ChecklistAnswerRequest.AnswerItem>> grouped =
                request.getAnswers().stream()
                        .collect(Collectors.groupingBy(ChecklistAnswerRequest.AnswerItem::getQuestionId));

        for (Map.Entry<Long, List<ChecklistAnswerRequest.AnswerItem>> entry : grouped.entrySet()) {

            Long questionId = entry.getKey();
            List<ChecklistAnswerRequest.AnswerItem> answerItems = entry.getValue();

            ChecklistQuestion question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_QUESTION_NOT_FOUND));

            for (ChecklistAnswerRequest.AnswerItem item : answerItems) {

                ChecklistOption option = null;

                // 옵션 검증
                if (item.getOptionId() != null) {
                    option = optionRepository.findById(item.getOptionId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_OPTION_NOT_FOUND));

                    if (!option.getQuestion().getId().equals(questionId)) {
                        throw new BusinessException(ErrorCode.CHECKLIST_OPTION_NOT_IN_QUESTION);
                    }
                }

                // 검증
                if (option != null && option.getHasInput() && item.getAnswerText() == null) {
                    throw new BusinessException(ErrorCode.REQUIRED_ANSWER_INPUT);
                }

                if (option != null && !option.getHasInput() && item.getAnswerText() != null) {
                    throw new BusinessException(ErrorCode.INVALID_ANSWER_INPUT);
                }

                // Answer 생성
                ChecklistAnswer newAnswer = ChecklistAnswer.builder()
                        .checklist(checklist)
                        .question(question)
                        .selectedOption(option)
                        .answerText(item.getAnswerText())
                        .answeredBy(user)
                        .answeredAt(LocalDateTime.now())
                        .build();

                answerRepository.save(newAnswer);
            }
        }

        // 제출 완료 후 lock
        checklist.lockChecklist();

        eventPublisher.publishEvent(new ChecklistSubmittedEvent(
                this,
                checklist.getId(),
                checklist.getStep().getProject(),
                user,
                ip,
                checklist.getTitle()
        ));

        return checklist.getId();
    }
}
