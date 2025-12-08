package com.rdc.weflow_server.dto.checklist.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class ChecklistCreateRequest {
    private Long stepId;
    private String title;
    private String description;
    private String category;
    private List<QuestionCreateRequest> questions;

    @Getter
    public static class QuestionCreateRequest {
        private String questionText;
        private String questionType; // "SINGLE" | "TEXT"
        private Integer orderIndex;
        private List<OptionCreateRequest> options;
    }

    @Getter
    public static class OptionCreateRequest {
        private String optionText;
        private Integer orderIndex;
        private Boolean hasInput;
    }
}


