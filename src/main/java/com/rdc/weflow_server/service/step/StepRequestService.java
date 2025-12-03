package com.rdc.weflow_server.service.step;

import com.rdc.weflow_server.dto.step.StepRequestCreateRequest;
import com.rdc.weflow_server.dto.step.StepRequestListResponse;
import com.rdc.weflow_server.dto.step.StepRequestResponse;
import com.rdc.weflow_server.dto.step.StepRequestSummaryResponse;
import com.rdc.weflow_server.entity.step.Step;
import com.rdc.weflow_server.entity.step.StepRequest;
import com.rdc.weflow_server.entity.step.StepRequestHistory;
import com.rdc.weflow_server.entity.step.StepRequestStatus;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.entity.user.UserRole;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.project.ProjectMemberRepository;
import com.rdc.weflow_server.repository.step.StepRequestHistoryRepository;
import com.rdc.weflow_server.repository.step.StepRequestRepository;
import com.rdc.weflow_server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StepRequestService {

    private final StepRequestRepository stepRequestRepository;
    private final StepRequestHistoryRepository stepRequestHistoryRepository;
    private final StepService stepService;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public StepRequestResponse createRequest(Long stepId, Long currentUserId, StepRequestCreateRequest request) {
        Step step = stepService.getStepOrThrow(stepId);
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 시스템 관리자는 예외적으로 허용
        if (user.getRole() != UserRole.SYSTEM_ADMIN) {
            // 개발사 소속인지 확인
            if (user.getRole() != UserRole.AGENCY) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }

            // 프로젝트 활성 멤버인지 확인 (deletedAt 검사 포함)
            boolean isActiveMember = projectMemberRepository.findByProjectIdAndUserId(step.getProject().getId(), currentUserId)
                    .filter(pm -> pm.getDeletedAt() == null)
                    .isPresent();
            if (!isActiveMember) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
        }

        StepRequest stepRequest = StepRequest.builder()
                .requestTitle(request.getTitle())
                .requestDescription(request.getDescription())
                .status(StepRequestStatus.REQUESTED)
                .step(step)
                .requestedBy(user)
                .build();

        StepRequest saved = stepRequestRepository.save(stepRequest);
        // REQUEST_UPDATE: 제목/설명 초기값 기록(before=null, after=요청 내용 요약)
        saveHistory(stepRequest, StepRequestHistory.HistoryType.REQUEST_UPDATE, "request", null, toRequestContent(request), user);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public StepRequestResponse getRequest(Long requestId) {
        StepRequest stepRequest = stepRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STEP_REQUEST_NOT_FOUND));
        return toResponse(stepRequest);
    }

    @Transactional(readOnly = true)
    public StepRequestListResponse getRequestsByStep(Long stepId) {
        // 삭제된 Step이면 조회도 404 처리
        stepService.getStepOrThrow(stepId);
        List<StepRequest> requests = stepRequestRepository.findByStep_Id(stepId);
        List<StepRequestSummaryResponse> summaries = requests.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());

        return StepRequestListResponse.builder()
                .totalCount((long) summaries.size())
                .page(0)
                .size(summaries.size())
                .stepRequestSummaryResponses(summaries)
                .build();
    }

    @Transactional(readOnly = true)
    public StepRequestListResponse getRequestsByProject(Long projectId) {
        // 정책: 삭제된 Step에 속한 Request도 프로젝트 히스토리로 조회 가능
        List<StepRequest> requests = stepRequestRepository.findByStep_Project_Id(projectId);
        List<StepRequestSummaryResponse> summaries = requests.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());

        return StepRequestListResponse.builder()
                .totalCount((long) summaries.size())
                .page(0)
                .size(summaries.size())
                .stepRequestSummaryResponses(summaries)
                .build();
    }

    // 현재 정책: REQUESTED 상태에서만 취소 가능, 상태를 CANCELED로 전이하며 히스토리에 남김
    public void cancelRequest(Long requestId, Long currentUserId) {
        StepRequest stepRequest = stepRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STEP_REQUEST_NOT_FOUND));
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Step step = stepRequest.getStep();
        if (step == null || step.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.STEP_NOT_FOUND);
        }

        boolean isSystemAdmin = user.getRole() == UserRole.SYSTEM_ADMIN;
        boolean isRequester = stepRequest.getRequestedBy() != null
                && stepRequest.getRequestedBy().getId().equals(currentUserId);

        boolean isDeveloperAdmin = projectMemberRepository
                .findByProjectIdAndUserId(step.getProject().getId(), currentUserId)
                .filter(pm -> pm.getDeletedAt() == null)
                .filter(pm -> pm.getUser().getRole() == UserRole.AGENCY)
                .filter(pm -> pm.getRole() == com.rdc.weflow_server.entity.project.ProjectRole.ADMIN)
                .isPresent();

        if (!isSystemAdmin && !isRequester && !isDeveloperAdmin) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (stepRequest.getStatus() != StepRequestStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.STEP_REQUEST_CANNOT_CANCEL);
        }

        StepRequestStatus beforeStatus = stepRequest.getStatus();
        stepRequest.updateStatus(StepRequestStatus.CANCELED);
        // REQUEST_UPDATE: 상태 전이 기록
        saveHistory(stepRequest, StepRequestHistory.HistoryType.REQUEST_UPDATE, "status", beforeStatus.name(), StepRequestStatus.CANCELED.name(), user);
    }

    private StepRequestResponse toResponse(StepRequest stepRequest) {
        return StepRequestResponse.builder()
                .id(stepRequest.getId())
                .title(stepRequest.getRequestTitle())
                .description(stepRequest.getRequestDescription())
                .status(stepRequest.getStatus())
                .decidedAt(stepRequest.getDecidedAt())
                .stepId(stepRequest.getStep() != null ? stepRequest.getStep().getId() : null)
                .projectId(stepRequest.getStep() != null && stepRequest.getStep().getProject() != null
                        ? stepRequest.getStep().getProject().getId() : null)
                .requestedBy(stepRequest.getRequestedBy() != null ? stepRequest.getRequestedBy().getId() : null)
                .createdAt(stepRequest.getCreatedAt())
                .build();
    }

    private StepRequestSummaryResponse toSummary(StepRequest stepRequest) {
        return StepRequestSummaryResponse.builder()
                .id(stepRequest.getId())
                .title(stepRequest.getRequestTitle())
                .status(stepRequest.getStatus())
                .createdAt(stepRequest.getCreatedAt())
                .decidedAt(stepRequest.getDecidedAt())
                .stepId(stepRequest.getStep() != null ? stepRequest.getStep().getId() : null)
                .stepTitle(stepRequest.getStep() != null ? stepRequest.getStep().getTitle() : null)
                .build();
    }

    private void saveHistory(StepRequest stepRequest, StepRequestHistory.HistoryType type, String fieldName, String beforeContent, String afterContent, User updatedBy) {
        StepRequestHistory history = StepRequestHistory.builder()
                .historyType(type)
                .fieldName(fieldName)
                .beforeContent(beforeContent)
                .afterContent(afterContent)
                .request(stepRequest)
                .updatedBy(updatedBy)
                .build();
        stepRequestHistoryRepository.save(history);
    }

    private String toRequestContent(StepRequestCreateRequest request) {
        // 단순 문자열 직렬화 (추후 JSON 포맷 필요 시 교체)
        return String.format("title=%s;description=%s", request.getTitle(), request.getDescription());
    }
}
