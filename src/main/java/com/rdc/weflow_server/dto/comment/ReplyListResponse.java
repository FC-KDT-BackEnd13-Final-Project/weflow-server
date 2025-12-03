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
public class ReplyListResponse {

    private List<ReplyDto> replies;
    private Integer totalCount;
    private Integer currentPage;
    private Integer totalPages;
    private Boolean hasNext;

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

}
