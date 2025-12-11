package com.rdc.weflow_server.controller.post;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.dto.post.*;
import com.rdc.weflow_server.entity.post.PostOpenStatus;
import com.rdc.weflow_server.entity.project.ProjectPhase;
import com.rdc.weflow_server.service.post.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
    public ApiResponse<PostDetailResponse> getPost(@PathVariable Long projectId, @PathVariable Long postId) {
        PostDetailResponse response = postService.getPost(projectId, postId);
        return ApiResponse.success("게시글 조회 성공", response);
    }

    /**
     * 게시글 리스트 조회
     */
    @GetMapping
    public ApiResponse<PostListResponse> getPosts(
            @PathVariable Long projectId,
            @RequestParam(required = false) ProjectPhase projectPhase,
            @RequestParam(required = false) PostOpenStatus openStatus,
            @RequestParam(required = false) Long stepId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        PostListResponse response = postService.getPosts(projectId, projectPhase, openStatus, stepId, pageable);
        return ApiResponse.success("게시글 목록 조회 성공", response);
    }

    /**
     * 게시글 작성
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostCreateResponse> createPost(
            @PathVariable Long projectId,
            @RequestBody PostCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        PostCreateResponse response = postService.createPost(projectId, request, httpRequest);
        return ApiResponse.success("게시글 작성 성공", response);
    }

    /**
     * 게시글 수정
     */
    @PatchMapping("/{postId}")
    public ApiResponse<PostDetailResponse> updatePost(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @RequestBody PostUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        PostDetailResponse response = postService.updatePost(projectId, postId, request, httpRequest);
        return ApiResponse.success("게시글 수정 성공", response);
    }

    /**
     * 게시글 삭제 (Soft Delete)
     */
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            HttpServletRequest httpRequest
    ) {
        postService.deletePost(projectId, postId, httpRequest);
        return ApiResponse.success("게시글 삭제 성공", null);
    }

    /**
     * 질문에 대한 답변 등록
     */
    @PostMapping("/{postId}/questions/{questionId}/answer")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostAnswerResponse> answerQuestion(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @PathVariable Long questionId,
            @RequestBody PostAnswerRequest request,
            HttpServletRequest httpRequest
    ) {
        PostAnswerResponse response = postService.answerQuestion(projectId, postId, questionId, request, httpRequest);
        return ApiResponse.success("답변 등록 성공", response);
    }

    /**
     * 게시글 승인 상태 변경 (CONFIRMED/REJECTED)
     */
    @PatchMapping("/{postId}/status")
    public ApiResponse<Void> updatePostStatus(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @RequestBody PostStatusUpdateRequest request
    ) {
        postService.updatePostStatus(projectId, postId, request);
        return ApiResponse.success("게시글 상태 변경 성공", null);
    }

    /**
     * 게시글 완료 (OPEN -> CLOSED)
     */
    @PatchMapping("/{postId}/close")
    public ApiResponse<Void> closePost(
            @PathVariable Long projectId,
            @PathVariable Long postId
    ) {
        postService.closePost(projectId, postId);
        return ApiResponse.success("게시글 완료 처리 성공", null);
    }

}
