package com.rdc.weflow_server.service.step;

import com.rdc.weflow_server.dto.step.StepRequestAnswerCreateRequest;
import com.rdc.weflow_server.dto.step.StepRequestAnswerResponse;
import com.rdc.weflow_server.entity.step.StepRequest;
import com.rdc.weflow_server.entity.step.StepRequestAnswer;
import com.rdc.weflow_server.entity.step.StepRequestAnswerType;
import com.rdc.weflow_server.entity.step.StepRequestHistory;
import com.rdc.weflow_server.entity.step.StepRequestStatus;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.step.StepRequestAnswerRepository;
import com.rdc.weflow_server.repository.step.StepRequestHistoryRepository;
import com.rdc.weflow_server.repository.step.StepRequestRepository;
import com.rdc.weflow_server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class StepRequestAnswerService {

    private final StepRequestRepository stepRequestRepository;
    private final StepRequestAnswerRepository stepRequestAnswerRepository;
    private final StepRequestHistoryRepository stepRequestHistoryRepository;
    private final UserRepository userRepository;

    public StepRequestAnswerResponse answerRequest(Long requestId, Long currentUserId, StepRequestAnswerCreateRequest request) {
        StepRequest stepRequest = stepRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STEP_REQUEST_NOT_FOUND));
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // TODO: 승인/반려는 "고객사 멤버(MEMBER)"만 가능
        //  - user.role == CLIENT
        //  - ProjectMember 존재 여부 확인

        // 삭제된 단계에 속한 승인요청은 더 이상 승인/반려 처리할 수 없음
        if (stepRequest.getStep() == null || stepRequest.getStep().getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.STEP_NOT_FOUND);
        }

        if (stepRequest.getStatus() != StepRequestStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.STEP_REQUEST_ALREADY_DECIDED);
        }

        stepRequestAnswerRepository.findByStepRequest_Id(requestId)
                .ifPresent(existing -> { throw new BusinessException(ErrorCode.STEP_ANSWER_ALREADY_EXISTS); });

        StepRequestStatus newStatus = mapAnswerToStatus(request.getResponse());
        StepRequestAnswer answer = StepRequestAnswer.builder()
                .response(request.getResponse())
                .reasonText(request.getReasonText())
                .stepRequest(stepRequest)
                .respondedBy(user)
                .build();

        stepRequest.updateStatus(newStatus);
        stepRequest.updateDecidedAt(LocalDateTime.now());

        StepRequestAnswer saved = stepRequestAnswerRepository.save(answer);
        // REASON_UPDATE: 반려/승인 사유 afterContent 기록
        saveReasonHistory(stepRequest, request.getReasonText(), user);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public StepRequestAnswerResponse getAnswer(Long requestId) {
        stepRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STEP_REQUEST_NOT_FOUND));

        StepRequestAnswer answer = stepRequestAnswerRepository.findByStepRequest_Id(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STEP_REQUEST_NOT_FOUND));
        // 삭제된 Step에 대한 승인 결과라도 조회는 허용 (히스토리 용)
        return toResponse(answer);
    }

    private StepRequestAnswerResponse toResponse(StepRequestAnswer answer) {
        return StepRequestAnswerResponse.builder()
                .id(answer.getId())
                .response(answer.getResponse())
                .requestId(answer.getStepRequest() != null ? answer.getStepRequest().getId() : null)
                .respondedBy(answer.getRespondedBy() != null ? answer.getRespondedBy().getId() : null)
                .createdAt(answer.getCreatedAt())
                .build();
    }

    private StepRequestStatus mapAnswerToStatus(StepRequestAnswerType response) {
        if (response == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return switch (response) {
            case APPROVE -> StepRequestStatus.APPROVED;
            case REJECT -> StepRequestStatus.REJECTED;
            case CHANGE_REQUEST -> StepRequestStatus.CHANGE_REQUESTED;
        };
    }

    private void saveReasonHistory(StepRequest stepRequest, String reasonText, User updatedBy) {
        StepRequestHistory history = StepRequestHistory.builder()
                .historyType(StepRequestHistory.HistoryType.REASON_UPDATE)
                .fieldName("reason")
                .beforeContent(null)
                .afterContent(reasonText)
                .request(stepRequest)
                .updatedBy(updatedBy)
                .build();
        stepRequestHistoryRepository.save(history);
    }
}
