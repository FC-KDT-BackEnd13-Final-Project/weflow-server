package com.rdc.weflow_server.dto.post;

import com.rdc.weflow_server.entity.post.PostApprovalStatus;
import com.rdc.weflow_server.entity.post.PostOpenStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostListResponse {

    private Long id;
    private String title;
    private PostDetailResponse.AuthorDto author;
    private LocalDateTime createdAt;
    private PostOpenStatus openStatus;
    private PostApprovalStatus status;
    private Integer fileCount;
    private Integer commentCount;
}
