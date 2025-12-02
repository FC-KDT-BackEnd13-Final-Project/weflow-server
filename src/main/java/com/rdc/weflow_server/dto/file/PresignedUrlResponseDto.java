package com.rdc.weflow_server.dto.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignedUrlResponseDto {
    private String url;   // 프론트가 PUT 요청할 URL
    private String key;   // S3에 저장될 파일 경로(key)
}