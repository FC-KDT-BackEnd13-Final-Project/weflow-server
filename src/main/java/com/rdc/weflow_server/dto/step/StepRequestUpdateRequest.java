package com.rdc.weflow_server.dto.step;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import org.hibernate.validator.constraints.URL;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StepRequestUpdateRequest {

    @Size(max = 255)
    private String title;
    private String description;

    @Size(max = 50)
    private List<FileRequest> files;

    @Size(max = 50)
    private List<LinkRequest> links;

    /** 유지할 기존 파일 ID 목록 (null이면 기존 전체교체 방식, 빈 배열이면 전체 삭제) */
    private List<Long> keepFileIds;

    /** 유지할 기존 링크 ID 목록 (null이면 기존 전체교체 방식, 빈 배열이면 전체 삭제) */
    private List<Long> keepLinkIds;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileRequest {
        private String fileName;
        private Long fileSize;
        private String filePath;
        private String contentType;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkRequest {
        @URL
        private String url;
    }
}
