package com.rdc.weflow_server.controller.attachment;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.dto.attachment.AttachmentFileRequest;
import com.rdc.weflow_server.dto.attachment.AttachmentLinkRequest;
import com.rdc.weflow_server.dto.attachment.AttachmentResponse;
import com.rdc.weflow_server.entity.attachment.Attachment;
import com.rdc.weflow_server.service.attachment.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Attachment", description = "첨부파일 API")
@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @Operation(summary = "파일 업로드 완료 후 메타데이터 저장")
    @PostMapping("/files")
    public ApiResponse<AttachmentResponse> uploadFile(@RequestBody AttachmentFileRequest request) {
        AttachmentResponse response = attachmentService.uploadFile(request);
        return ApiResponse.success("FILE_UPLOADED", response);
    }

    @Operation(summary = "링크 추가")
    @PostMapping("/links")
    public ApiResponse<AttachmentResponse> addLink(@RequestBody AttachmentLinkRequest request) {
        AttachmentResponse response = attachmentService.addLink(request);
        return ApiResponse.success("LINK_ADDED", response);
    }

    @Operation(summary = "첨부파일 다운로드 URL 조회")
    @GetMapping("/{attachmentId}/download-url")
    public ApiResponse<String> getDownloadUrl(@PathVariable Long attachmentId) {
        String downloadUrl = attachmentService.getDownloadUrl(attachmentId);
        return ApiResponse.success("DOWNLOAD_URL_GENERATED", downloadUrl);
    }

    @Operation(summary = "첨부파일 삭제")
    @DeleteMapping("/{attachmentId}")
    public ApiResponse<Void> deleteAttachment(@PathVariable Long attachmentId) {
        attachmentService.deleteAttachment(attachmentId);
        return ApiResponse.success("ATTACHMENT_DELETED", null);
    }

    @Operation(summary = "특정 대상의 첨부파일 목록 조회")
    @GetMapping
    public ApiResponse<List<AttachmentResponse>> getAttachments(
            @RequestParam Attachment.TargetType targetType,
            @RequestParam Long targetId
    ) {
        List<AttachmentResponse> responses = attachmentService.getAttachments(targetType, targetId);
        return ApiResponse.success("ATTACHMENTS_RETRIEVED", responses);
    }

    @Operation(summary = "첨부파일 상세 조회")
    @GetMapping("/{attachmentId}")
    public ApiResponse<AttachmentResponse> getAttachment(@PathVariable Long attachmentId) {
        AttachmentResponse response = attachmentService.getAttachment(attachmentId);
        return ApiResponse.success("ATTACHMENT_RETRIEVED", response);
    }

}
