package com.rdc.weflow_server.dto.step;

import com.rdc.weflow_server.entity.step.StepRequestAnswerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StepRequestAnswerCreateRequest {

    private StepRequestAnswerType response;
    private String reasonText;
    @Size(max = 50)
    private List<FileRequest> files;
    @Size(max = 50)
    private List<LinkRequest> links;

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
