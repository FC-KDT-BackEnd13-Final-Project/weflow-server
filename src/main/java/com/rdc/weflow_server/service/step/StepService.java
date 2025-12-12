package com.rdc.weflow_server.service.step;

import com.rdc.weflow_server.dto.step.StepCreateRequest;
import com.rdc.weflow_server.dto.step.StepListResponse;
import com.rdc.weflow_server.dto.step.StepOrderItem;
import com.rdc.weflow_server.dto.step.StepReorderRequest;
import com.rdc.weflow_server.dto.step.StepResponse;
import com.rdc.weflow_server.dto.step.StepUpdateRequest;
import com.rdc.weflow_server.entity.log.ActionType;
import com.rdc.weflow_server.entity.log.TargetTable;
import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.project.ProjectPhase;
import com.rdc.weflow_server.entity.step.Step;
import com.rdc.weflow_server.entity.step.StepStatus;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.service.log.AuditContext;
import com.rdc.weflow_server.service.log.ActivityLogService;
import com.rdc.weflow_server.service.permission.StepPermissionService;
import com.rdc.weflow_server.repository.checklist.ChecklistRepository;
import com.rdc.weflow_server.repository.post.PostRepository;
import com.rdc.weflow_server.repository.project.ProjectRepository;
import com.rdc.weflow_server.repository.step.StepRequestRepository;
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
    private final PostRepository postRepository;
    private final StepRequestRepository stepRequestRepository;
    private final ChecklistRepository checklistRepository;
    private final ActivityLogService activityLogService;
    private final StepPermissionService stepPermissionService;

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

    // 프로젝트 단계 목록 조회 (phase 필터)
    @Transactional(readOnly = true)
    public StepListResponse getStepsByProject(Long projectId, ProjectPhase phase) {
        List<Step> steps = stepRepository.findByProject_IdAndPhaseAndDeletedAtIsNullOrderByOrderIndexAsc(projectId, phase);

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
    public StepResponse createStep(Long projectId, StepCreateRequest request, AuditContext ctx) {
        Project project = getProjectOrThrow(projectId);
        User user = getUserOrThrow(ctx.userId());

        stepPermissionService.assertCanManageSteps(user, project.getId());

        if (stepRepository.existsByProject_IdAndTitleIgnoreCaseAndDeletedAtIsNull(projectId, request.getTitle())) {
            throw new BusinessException(ErrorCode.STEP_ALREADY_EXISTS);
        }

        ProjectPhase phase = request.getPhase() != null ? request.getPhase() : ProjectPhase.IN_PROGRESS;
        Integer orderIndex = resolveOrderIndex(projectId, request.getOrderIndex());
        StepStatus status = StepStatus.PENDING;

        Step step = Step.builder()
                .phase(phase)
                .title(request.getTitle())
                .description(request.getDescription())
                .orderIndex(orderIndex)
                .status(status)
                .project(project)
                .createdBy(user)
                .build();

        Step saved = stepRepository.save(step);
        activityLogService.createLog(
                ActionType.CREATE,
                TargetTable.STEP,
                saved.getId(),
                ctx.userId(),
                project.getId(),
                ctx.ipAddress()
        );
        return toStepResponse(saved);
    }

    // 프로젝트 생성 시 기본 단계 생성 (IN_PROGRESS 상위 흐름 하에 카테고리 순서대로 삽입)
    public void createDefaultStepsForProject(Project project, User creator) {
        if (project == null || project.getId() == null) {
            return;
        }
        // 이미 단계가 있다면 중복 생성하지 않는다.
        List<Step> existing = stepRepository.findByProject_IdAndDeletedAtIsNullOrderByOrderIndexAsc(project.getId());
        if (!existing.isEmpty()) {
            return;
        }

        ProjectPhase phase = ProjectPhase.IN_PROGRESS;
        StepStatus status = StepStatus.PENDING;

        List<Step> defaults = List.of(
                buildDefaultStep(project, creator, phase, status, 1, "요구사항 정의"),
                buildDefaultStep(project, creator, phase, status, 2, "화면 설계"),
                buildDefaultStep(project, creator, phase, status, 3, "디자인"),
                buildDefaultStep(project, creator, phase, status, 4, "퍼블리싱"),
                buildDefaultStep(project, creator, phase, status, 5, "개발"),
                buildDefaultStep(project, creator, phase, status, 6, "검수")
        );

        stepRepository.saveAll(defaults);
    }

    /* 단계 수정 (개발사 관리자)
    - PENDING: 자유롭게 수정
    - IN_PROGRESS: 삭제/순서 변경 막고, 이름/설명 수정 허용 (상위 카테고리/프로젝트 상태는 수정 불가)
    - WAITING_APPROVAL, APPROVED: 수정 불가
     */
    public StepResponse updateStep(Long stepId, StepUpdateRequest request, AuditContext ctx) {
        Step step = getStepOrThrow(stepId);
        User user = getUserOrThrow(ctx.userId());

        stepPermissionService.assertCanManageSteps(user, step.getProject().getId());

        StepStatus currentStatus = step.getStatus();
        if (currentStatus != StepStatus.PENDING) {
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

        activityLogService.createLog(
                ActionType.UPDATE,
                TargetTable.STEP,
                step.getId(),
                ctx.userId(),
                step.getProject().getId(),
                ctx.ipAddress()
        );
        return toStepResponse(step);
    }

    /*
     단계 삭제 (개발사 관리자)
     - PENDING 상태일 떄만 삭제 허용
     - 게시글/승인요청/체크리스트 연동 시 연결 데이터 있으면 삭제 금지 정책 추가 예정
     - 현재 구현: 하드 삭제 대신 deletedAt 값으로 soft delete 처리
     */
    public void deleteStep(Long stepId, AuditContext ctx) {
        Step step = getStepOrThrow(stepId);
        User user = getUserOrThrow(ctx.userId());

        stepPermissionService.assertCanManageSteps(user, step.getProject().getId());

        StepStatus status = step.getStatus();
        if (status != StepStatus.PENDING) {
            throw new BusinessException(ErrorCode.STEP_STATUS_INVALID);
        }

        boolean hasPosts = postRepository.existsByStepIdAndDeletedAtIsNull(stepId);
        boolean hasRequests = stepRequestRepository.existsByStep_Id(stepId);
        boolean hasChecklists = checklistRepository.existsByStep_Id(stepId);

        if (hasPosts || hasRequests || hasChecklists) {
            throw new BusinessException(ErrorCode.STEP_STATUS_INVALID);
        }

        step.softDelete();
        activityLogService.createLog(
                ActionType.DELETE,
                TargetTable.STEP,
                step.getId(),
                ctx.userId(),
                step.getProject().getId(),
                ctx.ipAddress()
        );
    }

    /*
     단계 순서 변경 (개발사 관리자)
     - PENDING 상태인 단계만 순서 변경 허용
     - 동일 phase 내에서만 순서 변경 가능
     */
    public void reorderSteps(Long projectID, StepReorderRequest request, AuditContext ctx) {
        User user = getUserOrThrow(ctx.userId());
        Project project = getProjectOrThrow(projectID);

        stepPermissionService.assertCanManageSteps(user, project.getId());

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

        // phase가 섞여 있으면 순서 변경 불가
        Set<ProjectPhase> phases = steps.stream()
                .map(Step::getPhase)
                .collect(Collectors.toSet());
        if (phases.size() > 1) {
            throw new BusinessException(ErrorCode.STEP_ORDER_INVALID);
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

        activityLogService.createLog(
                ActionType.UPDATE,
                TargetTable.STEP,
                null,
                ctx.userId(),
                project.getId(),
                ctx.ipAddress()
        );
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
                .updatedAt(step.getUpdatedAt())
                .build();
    }

    private Step buildDefaultStep(
            Project project,
            User creator,
            ProjectPhase phase,
            StepStatus status,
            Integer orderIndex,
            String title
    ) {
        return Step.builder()
                .phase(phase)
                .title(title)
                .description(null)
                .orderIndex(orderIndex)
                .status(status)
                .project(project)
                .createdBy(creator)
                .build();
    }
}
