package com.rdc.weflow_server.service.file;

import com.rdc.weflow_server.dto.file.PresignedUrlResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class S3FileService {

    private final S3Presigner presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public PresignedUrlResponseDto generatePresignedUrl(String key, String contentType) {
        // PUT 요청으로 업로드할 파일 정보
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        // Presigned URL 만료 시간 설정
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5)) // 5분 유효
                .putObjectRequest(objectRequest)
                .build();

        // Presigned URL 생성
        PresignedPutObjectRequest presignedRequest =
                presigner.presignPutObject(presignRequest);

        return new PresignedUrlResponseDto(
                presignedRequest.url().toString(),
                key
        );
    }
}
