package com.rdc.weflow_server.controller.post;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.dto.post.PostCreateRequest;
import com.rdc.weflow_server.dto.post.PostCreateResponse;
import com.rdc.weflow_server.dto.post.PostDetailResponse;
import com.rdc.weflow_server.dto.post.PostListResponse;
import com.rdc.weflow_server.dto.post.PostUpdateRequest;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import com.rdc.weflow_server.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/projects/{projectId}/posts")
public class PostController {

    private final PostService postService;

    /**
     * 게시글 상세 조회 (1개)
     */
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(@PathVariable Long projectId, @PathVariable Long postId) {
        PostDetailResponse response = postService.getPost(projectId, postId);
        return ResponseEntity.ok(ApiResponse.success("게시글 조회 성공", response));
    }

    /**
     * 게시글 리스트 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PostListResponse>> getPosts(
            @PathVariable Long projectId,
            @RequestParam(required = false) ProjectStatus projectStatus,
            @RequestParam(required = false) Long stepId
    ) {
        PostListResponse response = postService.getPosts(projectId, projectStatus, stepId);
        return ResponseEntity.ok(ApiResponse.success("게시글 목록 조회 성공", response));
    }

    /**
     * 게시글 작성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostCreateResponse>> createPost(
            @PathVariable Long projectId,
            @RequestBody PostCreateRequest request
    ) {
        PostCreateResponse response = postService.createPost(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("게시글 작성 성공", response));
    }

    /**
     * 게시글 수정
     */
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> updatePost(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @RequestBody PostUpdateRequest request
    ) {
        PostDetailResponse response = postService.updatePost(projectId, postId, request);
        return ResponseEntity.ok(ApiResponse.success("게시글 수정 성공", response));
    }

    /**
     * 게시글 삭제 (Soft Delete)
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long projectId,
            @PathVariable Long postId
    ) {
        postService.deletePost(projectId, postId);
        return ResponseEntity.ok(ApiResponse.success("게시글 삭제 성공", null));
    }

}
