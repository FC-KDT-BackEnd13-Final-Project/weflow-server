package com.rdc.weflow_server.service.step;

import com.rdc.weflow_server.dto.step.StepRequestCreateRequest;
import com.rdc.weflow_server.dto.step.StepRequestListResponse;
import com.rdc.weflow_server.dto.step.StepRequestResponse;
import com.rdc.weflow_server.dto.step.StepRequestSummaryResponse;
import com.rdc.weflow_server.dto.step.StepRequestUpdateRequest;
import com.rdc.weflow_server.dto.attachment.AttachmentSimpleResponse;
import com.rdc.weflow_server.entity.attachment.Attachment;
import com.rdc.weflow_server.entity.log.ActionType;
import com.rdc.weflow_server.entity.log.TargetTable;
import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.step.Step;
import com.rdc.weflow_server.entity.step.StepRequest;
import com.rdc.weflow_server.entity.step.StepRequestHistory;
import com.rdc.weflow_server.entity.step.StepRequestHistory.HistoryType;
import com.rdc.weflow_server.entity.step.StepRequestStatus;
import com.rdc.weflow_server.entity.step.StepStatus;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.entity.user.UserRole;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.entity.notification.NotificationType;
import com.rdc.weflow_server.service.notification.NotificationService;
import com.rdc.weflow_server.repository.attachment.AttachmentRepository;
import com.rdc.weflow_server.repository.project.ProjectMemberRepository;
import com.rdc.weflow_server.repository.project.ProjectRepository;
import com.rdc.weflow_server.repository.step.StepRequestHistoryRepository;
import com.rdc.weflow_server.repository.step.StepRequestAnswerRepository;
import com.rdc.weflow_server.repository.step.StepRequestRepository;
import com.rdc.weflow_server.repository.step.StepRepository;
import com.rdc.weflow_server.repository.user.UserRepository;
import com.rdc.weflow_server.service.log.ActivityLogService;
import com.rdc.weflow_server.service.file.S3FileService;
import com.rdc.weflow_server.service.log.AuditContext;
import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.service.permission.StepRequestPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class StepRequestService {

    private final StepRequestRepository stepRequestRepository;
    private final StepRequestHistoryRepository stepRequestHistoryRepository;
    private final StepService stepService;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final AttachmentRepository attachmentRepository;
    private final ActivityLogService activityLogService;
    private final S3FileService s3FileService;
    private final NotificationService notificationService;
    private final StepRepository stepRepository;
    private final StepRequestAnswerRepository stepRequestAnswerRepository;
    private final ProjectRepository projectRepository;
    private final StepRequestPermissionService stepRequestPermissionService;

    public StepRequestResponse createRequest(Long stepId, AuditContext ctx, StepRequestCreateRequest request) {
        Step step = stepService.getStepOrThrow(stepId);
        User user = userRepository.findById(ctx.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        stepRequestPermissionService.assertCanCreateRequest(user, step.getProject().getId());

        // 승인 완료된 단계에는 신규 요청 생성 불가
        if (step.getStatus() == StepStatus.APPROVED) {
            throw new BusinessException(ErrorCode.STEP_STATUS_INVALID);
        }

        validatePreviousStepApproved(step);

        StepRequest stepRequest = StepRequest.builder()
                .requestTitle(request.getTitle())
                .requestDescription(request.getDescription())
                .status(StepRequestStatus.REQUESTED)
                .step(step)
                .requestedBy(user)
                .build();

        StepRequest saved = stepRequestRepository.save(stepRequest);
        syncRequestFiles(saved, toFilePayloads(request.getFiles()), ctx);
        syncRequestLinks(saved, toLinkPayloads(request.getLinks()), ctx);

        // 단계 상태를 승인 대기로 변경 (기존이 아니면)
        if (step.getStatus() != StepStatus.WAITING_APPROVAL) {
            step.updateStatus(StepStatus.WAITING_APPROVAL);
        }
        // REQUEST_UPDATE: 제목/설명 초기값 기록(before=null, after=요청 내용 요약)
        saveHistory(stepRequest, StepRequestHistory.HistoryType.REQUEST_UPDATE, "request", null, toRequestContent(request), user);
        activityLogService.createLog(
                ActionType.SUBMIT,
                TargetTable.STEP_REQUEST,
                saved.getId(),
                ctx.userId(),
                step.getProject().getId(),
                ctx.ipAddress()
        );
        notifyClients(stepRequest, NotificationType.STEP_REQUEST, step.getTitle(), stepRequest.getRequestTitle());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public StepRequestResponse getRequest(Long requestId) {
        StepRequest stepRequest = stepRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STEP_REQUEST_NOT_FOUND));
        return toResponse(stepRequest);
    }

    // 승인 요청 수정 (요청자만, 승인 전 상태만)
    public StepRequestResponse updateRequest(Long requestId, AuditContext ctx, StepRequestUpdateRequest request) {
        StepRequest stepRequest = stepRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STEP_REQUEST_NOT_FOUND));
        User user = userRepository.findById(ctx.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Step step = stepRequest.getStep();
        if (step == null || step.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.STEP_NOT_FOUND);
        }

        stepRequestPermissionService.assertCanUpdateRequest(user, stepRequest);

        // 제목/설명 업데이트
        if (request.getTitle() != null) {
            if (request.getTitle().isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            stepRequest.updateTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            stepRequest.updateDescription(request.getDescription());
        }

        // 파일 첨부 동기화 (null이면 유지, 빈 리스트면 모두 제거)
        if (request.getFiles() != null) {
            syncRequestFiles(stepRequest, toFilePayloads(request.getFiles()), ctx);
        }

        // 링크 첨부 동기화 (null이면 유지, 빈 리스트면 모두 제거)
        if (request.getLinks() != null) {
            syncRequestLinks(stepRequest, toLinkPayloads(request.getLinks()), ctx);
        }

        // CHANGE_REQUESTED -> REQUESTED 재요청 전이
        if (stepRequest.getStatus() == StepRequestStatus.CHANGE_REQUESTED) {
            clearExistingAnswer(stepRequest);
            stepRequest.updateStatus(StepRequestStatus.REQUESTED);
            stepRequest.updateDecidedAt(null);
            stepRequest.updateDecidedBy(null);
            saveHistory(stepRequest, HistoryType.REQUEST_UPDATE, "status", StepRequestStatus.CHANGE_REQUESTED.name(), StepRequestStatus.REQUESTED.name(), user);
        }

        // 수정 이력 기록
        saveHistory(stepRequest, HistoryType.REQUEST_UPDATE, "request", null, toRequestContent(request.getTitle(), request.getDescription()), user);

        activityLogService.createLog(
                ActionType.UPDATE,
                TargetTable.STEP_REQUEST,
                stepRequest.getId(),
                ctx.userId(),
                step.getProject().getId(),
                ctx.ipAddress()
        );
        updateStepStatusBasedOnRequests(step);
        notifyClients(stepRequest, NotificationType.STEP_REQUEST, step.getTitle(), stepRequest.getRequestTitle());
        return toResponse(stepRequest);
    }

    @Transactional(readOnly = true)
    public StepRequestListResponse getRequestsByStep(Long stepId, int page, int size, CustomUserDetails user) {
        User currentUser = getUserOrThrow(user);
        // 삭제된 Step이면 조회도 404 처리
        Step step = stepService.getStepOrThrow(stepId);
        stepRequestPermissionService.assertCanViewRequests(currentUser, step.getProject().getId());
        var pageable = org.springframework.data.domain.PageRequest.of(page, size);
        var pageResult = stepRequestRepository.findByStep_IdOrderByCreatedAtDesc(stepId, pageable);
        List<StepRequestSummaryResponse> summaries = toSummaries(pageResult.getContent());

        return StepRequestListResponse.builder()
                .totalCount(pageResult.getTotalElements())
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .stepRequestSummaryResponses(summaries)
                .build();
    }

    @Transactional(readOnly = true)
    public StepRequestListResponse getRequestsByProject(Long projectId, int page, int size, CustomUserDetails user) {
        User currentUser = getUserOrThrow(user);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        if (project.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        stepRequestPermissionService.assertCanViewRequests(currentUser, projectId);
        var pageable = org.springframework.data.domain.PageRequest.of(page, size);
        var pageResult = stepRequestRepository.findByStep_Project_IdOrderByCreatedAtDesc(projectId, pageable);
        List<StepRequestSummaryResponse> summaries = toSummaries(pageResult.getContent());

        return StepRequestListResponse.builder()
                .totalCount(pageResult.getTotalElements())
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .stepRequestSummaryResponses(summaries)
                .build();
    }

    @Transactional(readOnly = true)
    public StepRequestListResponse getRequestsByMyProjects(Long userId, int page, int size, StepRequestStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        var pageable = org.springframework.data.domain.PageRequest.of(page, size);
        var pageResult = getStepRequestPageForUser(user, status, pageable);

        List<StepRequestSummaryResponse> summaries = toSummaries(pageResult.getContent());

        return StepRequestListResponse.builder()
                .totalCount(pageResult.getTotalElements())
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .stepRequestSummaryResponses(summaries)
                .build();
    }

    // 현재 정책: WAITING_APPROVAL 상태에서만 취소 가능, 상태를 CANCELED로 전이하며 히스토리에 남김
    public void cancelRequest(Long requestId, AuditContext ctx) {
        StepRequest stepRequest = stepRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STEP_REQUEST_NOT_FOUND));
        User user = userRepository.findById(ctx.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Step step = stepRequest.getStep();
        if (step == null || step.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.STEP_NOT_FOUND);
        }

        stepRequestPermissionService.assertCanCancelRequest(user, stepRequest);

        StepRequestStatus beforeStatus = stepRequest.getStatus();
        stepRequest.updateStatus(StepRequestStatus.CANCELED);
        // REQUEST_UPDATE: 상태 전이 기록
        saveHistory(stepRequest, StepRequestHistory.HistoryType.REQUEST_UPDATE, "status", beforeStatus.name(), StepRequestStatus.CANCELED.name(), user);
        refreshStepStatus(step);
        activityLogService.createLog(
                ActionType.DELETE,
                TargetTable.STEP_REQUEST,
                stepRequest.getId(),
                ctx.userId(),
                step.getProject().getId(),
                ctx.ipAddress()
        );
        notifyClients(stepRequest, NotificationType.STEP_REQUEST, step.getTitle(), "승인 요청이 취소되었습니다.");
    }

    private void syncRequestFiles(StepRequest stepRequest, List<SimpleFilePayload> files, AuditContext ctx) {
        if (files == null) {
            return;
        }

        attachmentRepository.deleteByTargetTypeAndTargetIdAndAttachmentType(
                Attachment.TargetType.STEP_REQUEST,
                stepRequest.getId(),
                Attachment.AttachmentType.FILE
        );

        if (files.isEmpty()) {
            return;
        }

        for (SimpleFilePayload file : files) {
            validateFilePayload(file);
            Attachment attachment = Attachment.builder()
                    .targetType(Attachment.TargetType.STEP_REQUEST)
                    .targetId(stepRequest.getId())
                    .attachmentType(Attachment.AttachmentType.FILE)
                    .fileName(file.fileName())
                    .fileSize(file.fileSize())
                    .filePath(file.filePath())
                    .contentType(file.contentType())
                    .build();
            attachmentRepository.save(attachment);
            activityLogService.createLog(
                    ActionType.UPLOAD,
                    TargetTable.ATTACHMENT,
                    attachment.getId(),
                    ctx.userId(),
                    stepRequest.getStep().getProject().getId(),
                    ctx.ipAddress()
            );
        }
    }

    private void syncRequestLinks(StepRequest stepRequest, List<SimpleLinkPayload> links, AuditContext ctx) {
        if (links == null) {
            return;
        }

        attachmentRepository.deleteByTargetTypeAndTargetIdAndAttachmentType(
                Attachment.TargetType.STEP_REQUEST,
                stepRequest.getId(),
                Attachment.AttachmentType.LINK
        );

        if (links.isEmpty()) {
            return;
        }

        for (SimpleLinkPayload link : links) {
            validateLinkPayload(link);
            Attachment attachment = Attachment.builder()
                    .targetType(Attachment.TargetType.STEP_REQUEST)
                    .targetId(stepRequest.getId())
                    .attachmentType(Attachment.AttachmentType.LINK)
                    .url(link.url())
                    .build();
            attachmentRepository.save(attachment);
            activityLogService.createLog(
                    ActionType.UPLOAD,
                    TargetTable.ATTACHMENT,
                    attachment.getId(),
                    ctx.userId(),
                    stepRequest.getStep().getProject().getId(),
                    ctx.ipAddress()
            );
        }
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
                .requestedByName(stepRequest.getRequestedBy() != null ? stepRequest.getRequestedBy().getName() : null)
                .decidedBy(stepRequest.getDecidedBy() != null ? stepRequest.getDecidedBy().getId() : null)
                .decidedByName(stepRequest.getDecidedBy() != null ? stepRequest.getDecidedBy().getName() : null)
                .decisionReason(stepRequest.getHistories().stream()
                        .filter(h -> h.getHistoryType() == HistoryType.REASON_UPDATE)
                        .reduce((first, second) -> second)
                        .map(StepRequestHistory::getAfterContent)
                        .orElse(null))
                .attachments(getAttachments(stepRequest))
                .createdAt(stepRequest.getCreatedAt())
                .build();
    }

    private StepRequestSummaryResponse toSummary(StepRequest stepRequest, boolean hasAttachment) {
        return StepRequestSummaryResponse.builder()
                .id(stepRequest.getId())
                .title(stepRequest.getRequestTitle())
                .status(stepRequest.getStatus())
                .createdAt(stepRequest.getCreatedAt())
                .decidedAt(stepRequest.getDecidedAt())
                .projectId(stepRequest.getStep() != null && stepRequest.getStep().getProject() != null
                        ? stepRequest.getStep().getProject().getId() : null)
                .projectName(stepRequest.getStep() != null && stepRequest.getStep().getProject() != null
                        ? stepRequest.getStep().getProject().getName() : null)
                .phase(stepRequest.getStep() != null ? stepRequest.getStep().getPhase() : null)
                .stepId(stepRequest.getStep() != null ? stepRequest.getStep().getId() : null)
                .stepTitle(stepRequest.getStep() != null ? stepRequest.getStep().getTitle() : null)
                .requestedBy(stepRequest.getRequestedBy() != null ? stepRequest.getRequestedBy().getId() : null)
                .requestedByName(stepRequest.getRequestedBy() != null ? stepRequest.getRequestedBy().getName() : null)
                .hasAttachment(hasAttachment)
                .build();
    }

    private List<AttachmentSimpleResponse> getAttachments(StepRequest stepRequest) {
        List<Attachment> attachments = attachmentRepository.findByTargetTypeAndTargetId(Attachment.TargetType.STEP_REQUEST, stepRequest.getId());
        return attachments.stream()
                .map(this::toAttachmentSimpleResponse)
                .collect(Collectors.toList());
    }

    private List<StepRequestSummaryResponse> toSummaries(List<StepRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = requests.stream()
                .map(StepRequest::getId)
                .filter(Objects::nonNull)
                .toList();

        Set<Long> requestIdsWithAttachments = new HashSet<>(
                attachmentRepository.findTargetIdsWithAttachments(
                        Attachment.TargetType.STEP_REQUEST,
                        requestIds
                )
        );

        return requests.stream()
                .map(req -> toSummary(req, req != null && requestIdsWithAttachments.contains(req.getId())))
                .collect(Collectors.toList());
    }

    private Page<StepRequest> getStepRequestPageForUser(User user, StepRequestStatus status, Pageable pageable) {
        if (user.getRole() == UserRole.SYSTEM_ADMIN) {
            return status != null
                    ? stepRequestRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                    : stepRequestRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        List<Long> projectIds = projectMemberRepository.findActiveProjectIdsByUserId(user.getId());
        if (projectIds.isEmpty()) {
            return Page.empty(pageable);
        }

        return status != null
                ? stepRequestRepository.findByStep_Project_IdInAndStatusOrderByCreatedAtDesc(projectIds, status, pageable)
                : stepRequestRepository.findByStep_Project_IdInOrderByCreatedAtDesc(projectIds, pageable);
    }

    private User getUserOrThrow(CustomUserDetails user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private AttachmentSimpleResponse toAttachmentSimpleResponse(Attachment attachment) {
        String url = attachment.getAttachmentType() == Attachment.AttachmentType.FILE && attachment.getFilePath() != null
                ? s3FileService.generateDownloadPresignedUrl(attachment.getFilePath(), attachment.getFileName())
                : attachment.getUrl();
        return AttachmentSimpleResponse.from(attachment, url);
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

    private void notifyClients(StepRequest stepRequest, NotificationType type, String title, String message) {
        if (stepRequest == null || stepRequest.getStep() == null || stepRequest.getStep().getProject() == null) {
            return;
        }
        List<User> receivers = projectMemberRepository.findByProjectIdAndDeletedAtIsNull(stepRequest.getStep().getProject().getId())
                .stream()
                .map(pm -> pm.getUser())
                .filter(u -> u != null && u.getRole() == UserRole.CLIENT)
                .toList();

        receivers.forEach(receiver ->
                notificationService.send(
                        receiver,
                        type,
                        String.format("승인요청 - %s", title),
                        message,
                        stepRequest.getStep().getProject(),
                        null,
                        stepRequest
                )
        );
    }

    public void refreshStepStatus(Step step) {
        updateStepStatusBasedOnRequests(step);
    }

    private void validatePreviousStepApproved(Step step) {
        if (step == null || step.getProject() == null) {
            throw new BusinessException(ErrorCode.INVALID_STEP_ORDER);
        }

        // 전체 단계 목록을 phasePriority → orderIndex → id 기준으로 정렬
        List<Step> orderedSteps = stepRepository.findByProject_IdAndDeletedAtIsNullOrderByOrderIndexAsc(
                step.getProject().getId()
        );

        int index = -1;
        for (int i = 0; i < orderedSteps.size(); i++) {
            if (orderedSteps.get(i).getId().equals(step.getId())) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            throw new BusinessException(ErrorCode.INVALID_STEP_ORDER);
        }
        if (index == 0) {
            return; // 첫 단계는 이전 단계 없음
        }

        Step previousStep = orderedSteps.get(index - 1);
        if (previousStep.getStatus() != StepStatus.APPROVED) {
            throw new BusinessException(ErrorCode.PREVIOUS_STEP_NOT_APPROVED);
        }
    }

    public void updateStepStatusBasedOnRequests(Step step) {
        if (step == null || step.getId() == null) {
            return;
        }

        List<StepRequestStatus> inProgressStatuses = List.of(
                StepRequestStatus.REQUESTED,
                StepRequestStatus.CHANGE_REQUESTED
        );

        boolean hasInProgress = stepRequestRepository.existsByStep_IdAndStatusIn(step.getId(), inProgressStatuses);
        if (hasInProgress) {
            step.updateStatus(StepStatus.WAITING_APPROVAL);
            return;
        }

        boolean hasApproved = stepRequestRepository.existsByStep_IdAndStatus(step.getId(), StepRequestStatus.APPROVED);
        if (hasApproved) {
            step.updateStatus(StepStatus.APPROVED);
            return;
        }

        step.updateStatus(StepStatus.PENDING);
    }

    private List<SimpleFilePayload> toFilePayloads(List<?> files) {
        if (files == null) {
            return null;
        }
        return files.stream()
                .flatMap(f -> {
                    if (f instanceof StepRequestCreateRequest.FileRequest fr) {
                        return Stream.of(new SimpleFilePayload(fr.getFileName(), fr.getFileSize(), fr.getFilePath(), fr.getContentType()));
                    }
                    if (f instanceof StepRequestUpdateRequest.FileRequest fr) {
                        return Stream.of(new SimpleFilePayload(fr.getFileName(), fr.getFileSize(), fr.getFilePath(), fr.getContentType()));
                    }
                    return Stream.empty();
                })
                .toList();
    }

    private List<SimpleLinkPayload> toLinkPayloads(List<?> links) {
        if (links == null) {
            return null;
        }
        return links.stream()
                .flatMap(l -> {
                    if (l instanceof StepRequestCreateRequest.LinkRequest lr) {
                        return Stream.of(new SimpleLinkPayload(lr.getUrl()));
                    }
                    if (l instanceof StepRequestUpdateRequest.LinkRequest lr) {
                        return Stream.of(new SimpleLinkPayload(lr.getUrl()));
                    }
                    return Stream.empty();
                })
                .toList();
    }

    private record SimpleFilePayload(String fileName, Long fileSize, String filePath, String contentType) {}

    private record SimpleLinkPayload(String url) {}

    private void validateFilePayload(SimpleFilePayload file) {
        if (file == null || file.filePath() == null || file.filePath().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (file.filePath().startsWith("http://") || file.filePath().startsWith("https://")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        // DB 컬럼 길이 고려
        if (file.filePath().length() > 500) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateLinkPayload(SimpleLinkPayload link) {
        if (link == null || link.url() == null || link.url().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (!(link.url().startsWith("http://") || link.url().startsWith("https://"))) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String toRequestContent(StepRequestCreateRequest request) {
        // 단순 문자열 직렬화 (추후 JSON 포맷 필요 시 교체)
        return String.format("title=%s;description=%s", request.getTitle(), request.getDescription());
    }

    private String toRequestContent(String title, String description) {
        return String.format("title=%s;description=%s", title, description);
    }

    private void clearExistingAnswer(StepRequest stepRequest) {
        if (stepRequest == null || stepRequest.getId() == null) {
            return;
        }

        stepRequestAnswerRepository.findByStepRequest_Id(stepRequest.getId())
                .ifPresent(answer -> {
                    attachmentRepository.deleteByTargetTypeAndTargetId(
                            Attachment.TargetType.STEP_REQUEST_ANSWER,
                            answer.getId()
                    );
                    stepRequestAnswerRepository.delete(answer);
                });
    }
}
