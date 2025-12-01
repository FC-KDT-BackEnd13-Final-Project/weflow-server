package com.rdc.weflow_server.controller.post;

import com.rdc.weflow_server.dto.post.PostCreateRequest;
import com.rdc.weflow_server.dto.post.PostDetailResponse;
import com.rdc.weflow_server.dto.post.PostListResponse;
import com.rdc.weflow_server.entity.step.Phase;
import com.rdc.weflow_server.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class PostController {

    private final PostService postService;

    /**
     * 게시글 상세 조회 (1개)
     */
    @GetMapping("/api/projects/{projectId}/posts/{postId}")
    public ResponseEntity<PostDetailResponse> getPost(@PathVariable Long projectId, @PathVariable Long postId) {
        PostDetailResponse response = postService.getPost(projectId, postId);
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 리스트 조회
     */
    @GetMapping("/api/projects/{projectId}/posts")
    public ResponseEntity<List<PostListResponse>> getPosts(
            @PathVariable Long projectId,
            @RequestParam(required = false) Phase phase,
            @RequestParam(required = false) Long stepId
    ) {
        List<PostListResponse> responses = postService.getPosts(projectId, phase, stepId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 게시글 작성
     */
    @PostMapping("/api/projects/{projectId}/posts")
    public ResponseEntity<PostDetailResponse> createPost(
            @PathVariable Long projectId,
            @RequestBody PostCreateRequest request
    ) {
        PostDetailResponse response = postService.createPost(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
