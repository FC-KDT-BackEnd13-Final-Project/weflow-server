package com.rdc.weflow_server.dto.post;

import com.rdc.weflow_server.entity.post.PostApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostStatusUpdateRequest {

    private PostApprovalStatus status;

}
