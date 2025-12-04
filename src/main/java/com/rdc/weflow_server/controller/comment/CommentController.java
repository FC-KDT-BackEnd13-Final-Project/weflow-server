package com.rdc.weflow_server.controller.comment;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.dto.comment.CommentCreateRequest;
import com.rdc.weflow_server.dto.comment.CommentCreateResponse;
import com.rdc.weflow_server.dto.comment.CommentListResponse;
import com.rdc.weflow_server.dto.comment.ReplyListResponse;
import com.rdc.weflow_server.service.comment.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 목록 조회
     */
    @GetMapping("/api/posts/{postId}/comments")
    public ApiResponse<CommentListResponse> getComments(@PathVariable Long postId) {
        CommentListResponse response = commentService.getComments(postId);
        return ApiResponse.success("댓글 목록 조회 성공", response);
    }

    /**
     * 댓글 작성
     */
    @PostMapping("/api/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentCreateResponse> createComment(
            @PathVariable Long postId,
            @RequestBody CommentCreateRequest request
    ) {
        CommentCreateResponse response = commentService.createComment(postId, request);
        return ApiResponse.success("댓글 작성 성공", response);
    }

    /**
     * 대댓글 페이징 조회
     */
    @GetMapping("/api/comments/{commentId}/replies")
    public ApiResponse<ReplyListResponse> getReplies(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ReplyListResponse response = commentService.getReplies(commentId, page, size);
        return ApiResponse.success("대댓글 조회 성공", response);
    }

    /**
     * 대댓글 작성
     */
    @PostMapping("/api/comments/{commentId}/replies")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentCreateResponse> createReply(
            @PathVariable Long commentId,
            @RequestBody CommentCreateRequest request
    ) {
        CommentCreateResponse response = commentService.createReply(commentId, request);
        return ApiResponse.success("대댓글 작성 성공", response);
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/api/comments/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ApiResponse.success("댓글 삭제 성공", null);
    }

}
