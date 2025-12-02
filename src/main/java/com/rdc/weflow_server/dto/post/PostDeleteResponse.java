package com.rdc.weflow_server.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDeleteResponse {

    private boolean success;
    private Data data;
    private String error;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private Long postId;
        private LocalDateTime deletedAt;
    }
}
