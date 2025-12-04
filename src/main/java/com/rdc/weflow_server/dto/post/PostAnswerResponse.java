package com.rdc.weflow_server.dto.post;

import com.rdc.weflow_server.entity.post.AnswerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostAnswerResponse {

    private Long answerId;
    private AnswerType answerType;
    private String content;
    private RespondentDto respondent;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RespondentDto {
        private Long memberId;
        private String name;
        private String role;
    }

}
