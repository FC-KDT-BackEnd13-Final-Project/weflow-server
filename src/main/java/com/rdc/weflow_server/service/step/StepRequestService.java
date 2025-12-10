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
import com.rdc.weflow_server.repository.step.StepRequestHistoryRepository;
import com.rdc.weflow_server.repository.step.StepRequestRepository;
import com.rdc.weflow_server.repository.user.UserRepository;
import com.rdc.weflow_server.service.log.ActivityLogService;
import com.rdc.weflow_server.service.file.S3FileService;
import com.rdc.weflow_server.service.log.AuditContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
    private final AttachmentRepository attachmentRepository;
    private final ActivityLogService activityLogService;
    private final S3FileService s3FileService;
    private final NotificationService notificationService;

    public StepRequestResponse createRequest(Long stepId, AuditContext ctx, StepRequestCreateRequest request) {
        Step step = stepService.getStepOrThrow(stepId);
        User user = userRepository.findById(ctx.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 시스템 관리자는 예외적으로 허용
        if (user.getRole() != UserRole.SYSTEM_ADMIN) {
            // 개발사 소속인지 확인
            if (user.getRole() != UserRole.AGENCY) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }

            // 프로젝트 활성 멤버인지 확인 (deletedAt 검사 포함)
            boolean isActiveMember = projectMemberRepository.findByProjectIdAndUserId(step.getProject().getId(), ctx.userId())
                    .filter(pm -> pm.getDeletedAt() == null)
                    .isPresent();
            if (!isActiveMember) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
        }

        // 승인 완료된 단계에는 신규 요청 생성 불가
        if (step.getStatus() == StepStatus.APPROVED) {
            throw new BusinessException(ErrorCode.STEP_STATUS_INVALID);
        }

        StepRequest stepRequest = StepRequest.builder()
                .requestTitle(request.getTitle())
                .requestDescription(request.getDescription())
                .status(StepRequestStatus.REQUESTED)
                .step(step)
                .requestedBy(user)
                .build();

        StepRequest saved = stepRequestRepository.save(stepRequest);
        attachFiles(saved, request.getAttachmentIds(), ctx);
        attachLinks(saved, request.getLinks(), ctx);

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

        if (!stepRequest.getStatus().isEditable()) {
            throw new BusinessException(ErrorCode.STEP_REQUEST_ALREADY_DECIDED);
        }

        boolean isSystemAdmin = user.getRole() == UserRole.SYSTEM_ADMIN;
        boolean isRequester = stepRequest.getRequestedBy() != null
                && stepRequest.getRequestedBy().getId().equals(ctx.userId());

        if (!isSystemAdmin && !isRequester) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

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
        if (request.getAttachmentIds() != null) {
            replaceFiles(stepRequest, request.getAttachmentIds(), ctx);
        }

        // 링크 첨부 동기화 (null이면 유지, 빈 리스트면 모두 제거)
        if (request.getLinks() != null) {
            replaceLinks(stepRequest, request.getLinks(), ctx);
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
        notifyClients(stepRequest, NotificationType.STEP_REQUEST, step.getTitle(), stepRequest.getRequestTitle());
        return toResponse(stepRequest);
    }

    @Transactional(readOnly = true)
    public StepRequestListResponse getRequestsByStep(Long stepId, int page, int size) {
        // 삭제된 Step이면 조회도 404 처리
        stepService.getStepOrThrow(stepId);
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
    public StepRequestListResponse getRequestsByProject(Long projectId, int page, int size) {
        // 정책: 삭제된 Step에 속한 Request도 프로젝트 히스토리로 조회 가능
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
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Long> projectIds = projectMemberRepository.findActiveProjectIdsByUserId(userId);
        if (projectIds.isEmpty()) {
            return StepRequestListResponse.builder()
                    .totalCount(0L)
                    .page(page)
                    .size(size)
                    .stepRequestSummaryResponses(List.of())
                    .build();
        }

        var pageable = org.springframework.data.domain.PageRequest.of(page, size);
        var pageResult = status != null
                ? stepRequestRepository.findByStep_Project_IdInAndStatusOrderByCreatedAtDesc(projectIds, status, pageable)
                : stepRequestRepository.findByStep_Project_IdInOrderByCreatedAtDesc(projectIds, pageable);

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

        boolean isSystemAdmin = user.getRole() == UserRole.SYSTEM_ADMIN;
        boolean isRequester = stepRequest.getRequestedBy() != null
                && stepRequest.getRequestedBy().getId().equals(ctx.userId());

        // 요청자 본인만 취소 가능 (시스템관리자는 예외 허용)
        if (!isSystemAdmin && !isRequester) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (stepRequest.getStatus() != StepRequestStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.STEP_REQUEST_CANNOT_CANCEL);
        }

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

    private void attachFiles(StepRequest stepRequest, List<Long> attachmentIds, AuditContext ctx) {
        if (attachmentIds == null || attachmentIds.isEmpty()) {
            return;
        }

        List<Attachment> attachments = attachmentRepository.findAllById(attachmentIds);
        if (attachments.size() != attachmentIds.size()) {
            throw new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND);
        }

        for (Attachment attachment : attachments) {
            if (attachment.getTargetType() != Attachment.TargetType.STEP_REQUEST) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            if (attachment.getTargetId() != null && !attachment.getTargetId().equals(stepRequest.getId())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            attachment.bindTo(Attachment.TargetType.STEP_REQUEST, stepRequest.getId());
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

    private void attachLinks(StepRequest stepRequest, List<String> links, AuditContext ctx) {
        if (links == null) {
            return;
        }
        if (links.isEmpty()) {
        List<Attachment> existingLinks = attachmentRepository.findByTargetTypeAndTargetId(
                        Attachment.TargetType.STEP_REQUEST,
                        stepRequest.getId())
                .stream()
                .filter(a -> a.getAttachmentType() == Attachment.AttachmentType.LINK)
                .toList();
        if (!existingLinks.isEmpty()) {
            attachmentRepository.deleteAll(existingLinks);
            for (Attachment link : existingLinks) {
                activityLogService.createLog(
                        ActionType.REMOVE,
                        TargetTable.ATTACHMENT,
                        link.getId(),
                        ctx.userId(),
                        stepRequest.getStep().getProject().getId(),
                        ctx.ipAddress()
                );
            }
            activityLogService.createLog(
                    ActionType.REMOVE,
                    TargetTable.STEP_REQUEST,
                    stepRequest.getId(),
                    ctx.userId(),
                    stepRequest.getStep().getProject().getId(),
                    ctx.ipAddress()
            );
        }
            return;
        }

        for (String link : links) {
            Attachment attachment = Attachment.builder()
                    .targetType(Attachment.TargetType.STEP_REQUEST)
                    .targetId(stepRequest.getId())
                    .attachmentType(Attachment.AttachmentType.LINK)
                    .url(link)
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

    private void replaceFiles(StepRequest stepRequest, List<Long> incomingIds, AuditContext ctx) {
        List<Attachment> existing = attachmentRepository.findByTargetTypeAndTargetId(Attachment.TargetType.STEP_REQUEST, stepRequest.getId())
                .stream()
                .filter(a -> a.getAttachmentType() == Attachment.AttachmentType.FILE)
                .collect(Collectors.toList());

        // 제거 대상: 기존 파일 중 incomingIds에 없는 것들
        existing.stream()
                .filter(a -> !incomingIds.contains(a.getId()))
                .forEach(a -> {
                    attachmentRepository.delete(a);
                    activityLogService.createLog(
                            ActionType.REMOVE,
                            TargetTable.ATTACHMENT,
                            a.getId(),
                            ctx.userId(),
                            stepRequest.getStep().getProject().getId(),
                            ctx.ipAddress()
                    );
                });

        attachFiles(stepRequest, incomingIds, ctx);
    }

    private void replaceLinks(StepRequest stepRequest, List<String> links, AuditContext ctx) {
        attachLinks(stepRequest, links, ctx);
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
        if (step == null) {
            return;
        }

        if (step.getStatus() == StepStatus.APPROVED) {
            return;
        }

        boolean hasRequested = stepRequestRepository.existsByStep_IdAndStatus(step.getId(), StepRequestStatus.REQUESTED);
        if (hasRequested) {
            step.updateStatus(StepStatus.WAITING_APPROVAL);
        } else {
            step.updateStatus(StepStatus.PENDING);
        }
    }

    private String toRequestContent(StepRequestCreateRequest request) {
        // 단순 문자열 직렬화 (추후 JSON 포맷 필요 시 교체)
        return String.format("title=%s;description=%s", request.getTitle(), request.getDescription());
    }

    private String toRequestContent(String title, String description) {
        return String.format("title=%s;description=%s", title, description);
    }
}
