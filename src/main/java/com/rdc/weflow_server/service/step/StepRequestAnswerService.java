package com.rdc.weflow_server.service.step;

import com.rdc.weflow_server.dto.attachment.AttachmentSimpleResponse;
import com.rdc.weflow_server.dto.step.StepRequestAnswerCreateRequest;
import com.rdc.weflow_server.dto.step.StepRequestAnswerResponse;
import com.rdc.weflow_server.entity.attachment.Attachment;
import com.rdc.weflow_server.entity.log.ActionType;
import com.rdc.weflow_server.entity.log.TargetTable;
import com.rdc.weflow_server.entity.notification.NotificationType;
import com.rdc.weflow_server.entity.step.StepRequest;
import com.rdc.weflow_server.entity.step.StepRequestAnswer;
import com.rdc.weflow_server.entity.step.StepRequestAnswerType;
import com.rdc.weflow_server.entity.step.StepRequestHistory;
import com.rdc.weflow_server.entity.step.StepRequestStatus;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.attachment.AttachmentRepository;
import com.rdc.weflow_server.repository.step.StepRequestAnswerRepository;
import com.rdc.weflow_server.repository.step.StepRequestHistoryRepository;
import com.rdc.weflow_server.repository.step.StepRequestRepository;
import com.rdc.weflow_server.repository.user.UserRepository;
import com.rdc.weflow_server.service.log.ActivityLogService;
import com.rdc.weflow_server.service.log.AuditContext;
import com.rdc.weflow_server.service.file.S3FileService;
import com.rdc.weflow_server.service.notification.NotificationService;
import com.rdc.weflow_server.service.permission.StepRequestPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StepRequestAnswerService {

    private final StepRequestRepository stepRequestRepository;
    private final StepRequestAnswerRepository stepRequestAnswerRepository;
    private final StepRequestHistoryRepository stepRequestHistoryRepository;
    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final StepRequestService stepRequestService;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;
    private final S3FileService s3FileService;
    private final StepRequestPermissionService stepRequestPermissionService;

    public StepRequestAnswerResponse answerRequest(Long requestId, AuditContext ctx, StepRequestAnswerCreateRequest request) {
        StepRequest stepRequest = stepRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STEP_REQUEST_NOT_FOUND));
        User user = userRepository.findById(ctx.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 삭제된 단계에 속한 승인요청은 더 이상 승인/반려 처리할 수 없음
        if (stepRequest.getStep() == null || stepRequest.getStep().getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.STEP_NOT_FOUND);
        }

        stepRequestPermissionService.assertCanAnswerRequest(user, stepRequest);

        // 반려/변경 요청 시 사유 필수
        if ((request.getResponse() == StepRequestAnswerType.REJECT
                || request.getResponse() == StepRequestAnswerType.CHANGE_REQUEST)
                && (request.getReasonText() == null || request.getReasonText().isBlank())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        clearExistingAnswer(requestId);

        StepRequestStatus newStatus = mapAnswerToStatus(request.getResponse());
        StepRequestAnswer answer = StepRequestAnswer.builder()
                .response(request.getResponse())
                .reasonText(request.getReasonText())
                .stepRequest(stepRequest)
                .respondedBy(user)
                .build();

        stepRequest.updateStatus(newStatus);
        stepRequest.updateDecidedAt(LocalDateTime.now());
        stepRequest.updateDecidedBy(user);

        stepRequestService.updateStepStatusBasedOnRequests(stepRequest.getStep());

        StepRequestAnswer saved = stepRequestAnswerRepository.save(answer);
        syncAnswerFiles(saved, toFilePayloads(request.getFiles()));
        syncAnswerLinks(saved, toLinkPayloads(request.getLinks()));
        // REASON_UPDATE: 반려/승인 사유 afterContent 기록
        saveReasonHistory(stepRequest, request.getReasonText(), user);
        activityLogService.createLog(
                mapAnswerToAction(request.getResponse()),
                TargetTable.STEP_RESPONSE,
                stepRequest.getId(),
                ctx.userId(),
                stepRequest.getStep().getProject().getId(),
                ctx.ipAddress()
        );
        notifyRequesterDecision(stepRequest, saved);
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
                .respondedByName(answer.getRespondedBy() != null ? answer.getRespondedBy().getName() : null)
                .reasonText(answer.getReasonText())
                .attachments(getAttachments(answer))
                .decidedAt(answer.getStepRequest() != null ? answer.getStepRequest().getDecidedAt() : null)
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

    private ActionType mapAnswerToAction(StepRequestAnswerType response) {
        if (response == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return switch (response) {
            case APPROVE -> ActionType.APPROVE;
            case REJECT -> ActionType.REJECT;
            case CHANGE_REQUEST -> ActionType.UPDATE;
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

    private void syncAnswerFiles(StepRequestAnswer answer, List<SimpleFilePayload> files) {
        if (files == null) {
            return;
        }

        attachmentRepository.deleteByTargetTypeAndTargetIdAndAttachmentType(
                Attachment.TargetType.STEP_REQUEST_ANSWER,
                answer.getId(),
                Attachment.AttachmentType.FILE
        );

        if (files.isEmpty()) {
            return;
        }

        for (SimpleFilePayload file : files) {
            validateFilePayload(file);
            Attachment attachment = Attachment.builder()
                    .targetType(Attachment.TargetType.STEP_REQUEST_ANSWER)
                    .targetId(answer.getId())
                    .attachmentType(Attachment.AttachmentType.FILE)
                    .fileName(file.fileName())
                    .fileSize(file.fileSize())
                    .filePath(file.filePath())
                    .contentType(file.contentType())
                    .build();
            attachmentRepository.save(attachment);
        }
    }

    private void syncAnswerLinks(StepRequestAnswer answer, List<SimpleLinkPayload> links) {
        if (links == null) {
            return;
        }

        attachmentRepository.deleteByTargetTypeAndTargetIdAndAttachmentType(
                Attachment.TargetType.STEP_REQUEST_ANSWER,
                answer.getId(),
                Attachment.AttachmentType.LINK
        );

        if (links.isEmpty()) {
            return;
        }

        for (SimpleLinkPayload link : links) {
            validateLinkPayload(link);
            Attachment attachment = Attachment.builder()
                    .targetType(Attachment.TargetType.STEP_REQUEST_ANSWER)
                    .targetId(answer.getId())
                    .attachmentType(Attachment.AttachmentType.LINK)
                    .url(link.url())
                    .build();
            attachmentRepository.save(attachment);
        }
    }

    private List<AttachmentSimpleResponse> getAttachments(StepRequestAnswer answer) {
        List<Attachment> attachments = attachmentRepository.findByTargetTypeAndTargetId(
                Attachment.TargetType.STEP_REQUEST_ANSWER,
                answer.getId()
        );
        return attachments.stream()
                .map(this::toAttachmentSimpleResponse)
                .toList();
    }

    private AttachmentSimpleResponse toAttachmentSimpleResponse(Attachment attachment) {
        String url = attachment.getAttachmentType() == Attachment.AttachmentType.FILE && attachment.getFilePath() != null
                ? s3FileService.generateDownloadPresignedUrl(attachment.getFilePath(), attachment.getFileName())
                : attachment.getUrl();
        return AttachmentSimpleResponse.from(attachment, url);
    }

    private List<SimpleFilePayload> toFilePayloads(List<StepRequestAnswerCreateRequest.FileRequest> files) {
        if (files == null) {
            return null;
        }
        return files.stream()
                .map(f -> new SimpleFilePayload(f.getFileName(), f.getFileSize(), f.getFilePath(), f.getContentType()))
                .toList();
    }

    private List<SimpleLinkPayload> toLinkPayloads(List<StepRequestAnswerCreateRequest.LinkRequest> links) {
        if (links == null) {
            return null;
        }
        return links.stream()
                .map(l -> new SimpleLinkPayload(l.getUrl()))
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
        if (file.filePath().length() > 500) { // DB 컬럼 길이
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

    private void clearExistingAnswer(Long requestId) {
        stepRequestAnswerRepository.findByStepRequest_Id(requestId)
                .ifPresent(existing -> {
                    attachmentRepository.deleteByTargetTypeAndTargetId(
                            Attachment.TargetType.STEP_REQUEST_ANSWER,
                            existing.getId()
                    );
                    stepRequestAnswerRepository.delete(existing);
                });
    }

    private void notifyRequesterDecision(StepRequest stepRequest, StepRequestAnswer answer) {
        if (stepRequest == null || stepRequest.getRequestedBy() == null) {
            return;
        }
        String title = String.format("승인결과 - %s", stepRequest.getStep() != null ? stepRequest.getStep().getTitle() : "");
        String message = String.format("요청: %s / 결과: %s", stepRequest.getRequestTitle(), answer.getResponse().name());
        notificationService.send(
                stepRequest.getRequestedBy(),
                NotificationType.STEP_DECISION,
                title,
                message,
                stepRequest.getStep() != null ? stepRequest.getStep().getProject() : null,
                null,
                stepRequest
        );
    }
}
