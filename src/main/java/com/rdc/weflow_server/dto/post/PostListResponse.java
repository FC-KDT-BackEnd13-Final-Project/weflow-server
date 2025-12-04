package com.rdc.weflow_server.dto.post;

import com.rdc.weflow_server.entity.post.PostApprovalStatus;
import com.rdc.weflow_server.entity.project.ProjectStatus;
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
public class PostListResponse {

    private List<PostItem> posts;
    private PageInfo pageInfo;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostItem {
        private Long postId;
        private String title;
        private PostApprovalStatus status;
        private ProjectStatus projectStatus;
        private Long stepId;
        private AuthorDto author;
        private Boolean hasFiles;
        private Boolean hasLinks;
        private Boolean hasQuestions;
        private Integer commentCount;
        private Integer replyCount;
        private Boolean isEdited;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
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

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo {
        private Integer currentPage;
        private Integer pageSize;
        private Long totalElements;
        private Integer totalPages;
        private Boolean hasNext;
        private Boolean hasPrevious;
    }
}
