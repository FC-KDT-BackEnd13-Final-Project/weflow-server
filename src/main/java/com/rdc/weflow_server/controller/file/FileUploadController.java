package com.rdc.weflow_server.controller.file;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.dto.file.PresignedUrlResponseDto;
import com.rdc.weflow_server.service.file.S3FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final S3FileService s3FileService;

    @GetMapping("/presigned-url")
    public ApiResponse<PresignedUrlResponseDto> getPresignedUrl(
            @RequestParam String key,
            @RequestParam String contentType
    ) {
        PresignedUrlResponseDto response = s3FileService.generatePresignedUrl(key, contentType);
        return ApiResponse.success("PRESIGNED_URL_ISSUED", response);
    }
}
