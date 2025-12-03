package com.rdc.weflow_server.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentListResponse {

    private List<CommentResponse> comments;
    private Integer totalCount;

}
