package com.rdc.weflow_server.service.attachment;

import com.rdc.weflow_server.dto.attachment.AttachmentFileRequest;
import com.rdc.weflow_server.dto.attachment.AttachmentLinkRequest;
import com.rdc.weflow_server.dto.attachment.AttachmentResponse;
import com.rdc.weflow_server.entity.attachment.Attachment;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.attachment.AttachmentRepository;
import com.rdc.weflow_server.service.file.S3FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final S3FileService s3FileService;

    /**
     * 파일 업로드 완료 후 첨부파일 메타데이터 저장
     */
    @Transactional
    public AttachmentResponse uploadFile(AttachmentFileRequest request) {
        Attachment attachment = Attachment.builder()
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .attachmentType(Attachment.AttachmentType.FILE)
                .filePath(request.getFilePath())
                .fileName(request.getFileName())
                .fileSize(request.getFileSize())
                .contentType(request.getContentType())
                .build();

        Attachment saved = attachmentRepository.save(attachment);
        return AttachmentResponse.from(saved);
    }

    /**
     * 링크 추가
     */
    @Transactional
    public AttachmentResponse addLink(AttachmentLinkRequest request) {
        Attachment attachment = Attachment.builder()
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .attachmentType(Attachment.AttachmentType.LINK)
                .url(request.getUrl())
                .build();

        Attachment saved = attachmentRepository.save(attachment);
        return AttachmentResponse.from(saved);
    }

    /**
     * 첨부파일 다운로드 URL 생성
     */
    public String getDownloadUrl(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND));

        if (attachment.getAttachmentType() != Attachment.AttachmentType.FILE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return s3FileService.generateDownloadPresignedUrl(attachment.getFilePath());
    }

    /**
     * 첨부파일 삭제 (DB + S3)
     */
    @Transactional
    public void deleteAttachment(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND));

        // S3 파일 삭제 (파일 타입인 경우에만)
        if (attachment.getAttachmentType() == Attachment.AttachmentType.FILE
                && attachment.getFilePath() != null) {
            s3FileService.deleteFile(attachment.getFilePath());
        }

        // DB에서 삭제
        attachmentRepository.delete(attachment);
    }

    /**
     * 특정 대상의 첨부파일 목록 조회
     */
    public List<AttachmentResponse> getAttachments(Attachment.TargetType targetType, Long targetId) {
        List<Attachment> attachments = attachmentRepository.findByTargetTypeAndTargetId(targetType, targetId);
        return attachments.stream()
                .map(AttachmentResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 첨부파일 상세 조회
     */
    public AttachmentResponse getAttachment(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND));
        return AttachmentResponse.from(attachment);
    }
}
