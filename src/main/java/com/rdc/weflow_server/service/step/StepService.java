package com.rdc.weflow_server.service.step;

import com.rdc.weflow_server.dto.step.StepCreateRequest;
import com.rdc.weflow_server.dto.step.StepListResponse;
import com.rdc.weflow_server.dto.step.StepOrderItem;
import com.rdc.weflow_server.dto.step.StepReorderRequest;
import com.rdc.weflow_server.dto.step.StepResponse;
import com.rdc.weflow_server.dto.step.StepUpdateRequest;
import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.step.Step;
import com.rdc.weflow_server.entity.step.StepStatus;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.project.ProjectRepository;
import com.rdc.weflow_server.repository.step.StepRepository;
import com.rdc.weflow_server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StepService {

    private final StepRepository stepRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    private void checkManagerPermission(Long currentUserId, Project project) {
        // TODO: 프로젝트 멤버/권한 구현 들어오면 실제 DEV_MANAGER 여부 체크
    }

    // 프로젝트 단계 목록 조회
    @Transactional(readOnly = true)
    public StepListResponse getStepsByProject(Long projectId) {
        List<Step> steps = stepRepository.findByProject_IdAndDeletedAtIsNullOrderByOrderIndexAsc(projectId);

        List<StepResponse> stepResponses = steps.stream()
                .map(this::toStepResponse)
                .collect(Collectors.toList());

        return StepListResponse.builder()
                .totalCount((long) steps.size())
                .page(0)
                .size(stepResponses.size())
                .steps(stepResponses)
                .build();
    }

    // 단일 단계 조회
    @Transactional(readOnly = true)
    public StepResponse getStep(Long stepId) {
        Step step = getStepOrThrow(stepId);
        return toStepResponse(step);
    }

    // 단계 생성 (개발사 관리자)
    public StepResponse createStep(Long projectId, Long currentUserId, StepCreateRequest request) {
        Project project = getProjectOrThrow(projectId);
        User user = getUserOrThrow(currentUserId);

        checkManagerPermission(currentUserId, project);

        if (request.getPhase() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (stepRepository.existsByProject_IdAndTitleIgnoreCaseAndDeletedAtIsNull(projectId, request.getTitle())) {
            throw new BusinessException(ErrorCode.STEP_ALREADY_EXISTS);
        }

        Integer orderIndex = resolveOrderIndex(projectId, request.getOrderIndex());
        StepStatus status = request.getStatus() != null ? request.getStatus() : StepStatus.PENDING;

        Step step = Step.builder()
                .phase(request.getPhase())
                .title(request.getTitle())
                .description(request.getDescription())
                .orderIndex(orderIndex)
                .status(status)
                .project(project)
                .createdBy(user)
                .build();

        Step saved = stepRepository.save(step);
        return toStepResponse(saved);
    }

    /* 단계 수정 (개발사 관리자)
    - PENDING: 자유롭게 수정
    - IN_PROGRESS: 삭제/순서 변경 막고, 이름/설명 수정 허용 (Phase는 시스템 고정값으로 수정 불가)
    - WAITING_APPROVAL, APPROVED: 수정 불가
     */
    public StepResponse updateStep(Long stepId, Long currentUserId, StepUpdateRequest request) {
        Step step = getStepOrThrow(stepId);
        User user = getUserOrThrow(currentUserId);

        checkManagerPermission(currentUserId, step.getProject());

        StepStatus currentStatus = step.getStatus();

        // 수정 불가 상태
        if (currentStatus == StepStatus.WAITING_APPROVAL || currentStatus == StepStatus.APPROVED) {
            throw new BusinessException(ErrorCode.STEP_STATUS_INVALID);
        }

        // 수정 가능 항목만 적용 (상태는 승인/요청 플로우에서 바꾸는 걸로)
        if (request.getTitle() != null) {
            if (stepRepository.existsByProject_IdAndTitleIgnoreCaseAndIdNotAndDeletedAtIsNull(
                    step.getProject().getId(), request.getTitle(), step.getId())) {
                throw new BusinessException(ErrorCode.STEP_ALREADY_EXISTS);
            }
            step.updateTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            step.updateDescription(request.getDescription());
        }

        return toStepResponse(step);
    }

    /*
     단계 삭제 (개발사 관리자)
     - PENDING 상태일 떄만 삭제 허용
     - 게시글/승인요청/체크리스트 연동 시 연결 데이터 있으면 삭제 금지 정책 추가 예정
     - 현재 구현: 하드 삭제 대신 deletedAt 값으로 soft delete 처리
     */
    public void deleteStep(Long stepId, Long currentUserId) {
        Step step = getStepOrThrow(stepId);
        User user = getUserOrThrow(currentUserId);

        checkManagerPermission(currentUserId, step.getProject());

        StepStatus status = step.getStatus();
        if (status != StepStatus.PENDING) {
            throw new BusinessException(ErrorCode.STEP_STATUS_INVALID);
        }

        // TODO: Post, StepRequest, Checklist Repository exists 체크 후 삭제 막기
        step.softDelete();
    }

    /*
     단계 순서 변경 (개발사 관리자)
     - PENDING 상태인 단계만 순서 변경 허용
     */
    public void reorderSteps(Long projectID, Long currentUserId, StepReorderRequest request) {
        User user = getUserOrThrow(currentUserId);
        Project project = getProjectOrThrow(projectID);

        checkManagerPermission(currentUserId, project);

        List<StepOrderItem> orderItems = request.getSteps();
        if (orderItems == null || orderItems.isEmpty()) {
            return;
        }

        Map<Long, Integer> orderMap = orderItems.stream()
                .collect(Collectors.toMap(
                        StepOrderItem::getStepId,
                        StepOrderItem::getOrderIndex,
                        (existing, duplicate) -> { throw new BusinessException(ErrorCode.STEP_ORDER_INVALID); },
                        LinkedHashMap::new));

        validateOrderItems(orderMap);

        List<Step> steps = stepRepository.findByProject_IdAndIdInAndDeletedAtIsNull(projectID, orderMap.keySet());
        if (steps.size() != orderMap.size()) {
            throw new BusinessException(ErrorCode.STEP_NOT_FOUND);
        }

        for (Step step : steps) {
            Integer newOrder = orderMap.get(step.getId());

            StepStatus status = step.getStatus();

            // 순서 변경 불가 상태
            if (status == StepStatus.IN_PROGRESS
                || status == StepStatus.WAITING_APPROVAL
                || status == StepStatus.APPROVED) {
                throw new BusinessException(ErrorCode.STEP_STATUS_INVALID);
            }

            step.updateOrderIndex(newOrder);
        }
    }

    private Integer resolveOrderIndex(Long projectId, Integer requestedOrderIndex) {
        if (requestedOrderIndex == null || requestedOrderIndex < 1) {
            Integer maxOrder = stepRepository.findMaxOrderIndexByProjectId(projectId);
            return (maxOrder == null ? 1 : maxOrder + 1);
        }
        if (stepRepository.existsByProject_IdAndOrderIndexAndDeletedAtIsNull(projectId, requestedOrderIndex)) {
            throw new BusinessException(ErrorCode.STEP_ORDER_INVALID);
        }
        return requestedOrderIndex;
    }

    private void validateOrderItems(Map<Long, Integer> orderMap) {
        if (orderMap.isEmpty()) {
            return;
        }
        Set<Integer> orderIndices = new HashSet<>();
        for (Map.Entry<Long, Integer> entry : orderMap.entrySet()) {
            Long stepId = entry.getKey();
            Integer orderIndex = entry.getValue();
            if (stepId == null || orderIndex == null || orderIndex < 1) {
                throw new BusinessException(ErrorCode.STEP_ORDER_INVALID);
            }
            if (!orderIndices.add(orderIndex)) {
                throw new BusinessException(ErrorCode.STEP_ORDER_INVALID);
            }
        }
    }

    public Step getStepOrThrow(Long stepId) {
        return stepRepository.findByIdAndDeletedAtIsNull(stepId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STEP_NOT_FOUND));
    }

    private Project getProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private StepResponse toStepResponse(Step step) {
        return StepResponse.builder()
                .id(step.getId())
                .phase(step.getPhase())
                .title(step.getTitle())
                .description(step.getDescription())
                .orderIndex(step.getOrderIndex())
                .status(step.getStatus())
                .projectId(step.getProject() != null ? step.getProject().getId() : null)
                .createdBy(step.getCreatedBy() != null ? step.getCreatedBy().getId() : null)
                .createdAt(step.getCreatedAt())
                .lastModifiedAt(step.getLastModifiedAt())
                .build();
    }
}
