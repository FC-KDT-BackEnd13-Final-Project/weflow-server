package com.rdc.weflow_server.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequest {

    private String title;
    private String content;
    private List<FileRequest> files;
    private List<LinkRequest> links;
    private List<QuestionRequest> questions;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileRequest {
        private Long fileId;  // 기존 파일 ID (있으면 기존 파일 유지, 없으면 새 파일)
        private String fileName;
        private Long fileSize;
        private String filePath;
        private String contentType;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkRequest {
        private Long linkId;  // 기존 링크 ID (있으면 기존 링크 유지, 없으면 새 링크)
        private String url;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionRequest {
        private String questionText;
        private String questionType; // SINGLE, MULTI, TEXT
        private List<QuestionOptionRequest> options;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionOptionRequest {
        private String optionText;
        private Boolean hasInput;
    }
}
