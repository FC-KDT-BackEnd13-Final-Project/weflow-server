package com.rdc.weflow_server.dto.post;

import com.rdc.weflow_server.entity.project.ProjectPhase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {

    private String title;
    private String content;
    private Long stepId;
    private Long parentPostId; // 답글인 경우
    private ProjectPhase projectPhase;
    private List<FileRequest> files;
    private List<LinkRequest> links;
    private List<QuestionRequest> questions;

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
        private String url;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionRequest {
        private String questionText;
        private String confirmLabel;
        private String rejectLabel;
    }
}
