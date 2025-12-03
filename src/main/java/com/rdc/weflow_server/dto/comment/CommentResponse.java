package com.rdc.weflow_server.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long commentId;
    private String content;
    private AuthorDto author;
    private LocalDateTime createdAt;
    private Integer replyCount; // 대댓글 개수
    private List<ReplyDto> replies; // 대댓글 미리보기 (최대 3개)

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorDto {
        private Long memberId;
        private String name;
        private String role;
        private String companyName;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplyDto {
        private Long commentId;
        private String content;
        private AuthorDto author;
        private LocalDateTime createdAt;
    }

}
