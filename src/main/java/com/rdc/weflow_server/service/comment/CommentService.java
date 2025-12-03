package com.rdc.weflow_server.service.comment;

import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.comment.*;
import com.rdc.weflow_server.entity.comment.Comment;
import com.rdc.weflow_server.entity.post.Post;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.entity.user.UserRole;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.comment.CommentRepository;
import com.rdc.weflow_server.repository.post.PostRepository;
import com.rdc.weflow_server.repository.project.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ProjectMemberRepository projectMemberRepository;

    /**
     * 댓글 목록 조회 (대댓글 미리보기 포함)
     */
    @Transactional(readOnly = true)
    public CommentListResponse getComments(Long postId) {
        // Post 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 권한 검증
        validateProjectAccess(post.getStep().getProject().getId());

        // 최상위 댓글들 조회
        List<Comment> comments = commentRepository.findByPostIdAndParentCommentIsNull(postId);

        // CommentResponse로 변환
        List<CommentResponse> commentResponses = comments.stream()
                .map(comment -> {
                    // 대댓글 개수
                    Integer replyCount = commentRepository.countByParentCommentId(comment.getId());

                    // 대댓글 미리보기 (최대 3개)
                    List<Comment> replies = commentRepository.findByParentCommentIdList(comment.getId());
                    List<CommentResponse.ReplyDto> replyDtos = replies.stream()
                            .limit(3)
                            .map(reply -> CommentResponse.ReplyDto.builder()
                                    .commentId(reply.getId())
                                    .content(reply.getContent())
                                    .author(buildAuthorDto(reply.getUser()))
                                    .createdAt(reply.getCreatedAt())
                                    .build())
                            .collect(Collectors.toList());

                    return CommentResponse.builder()
                            .commentId(comment.getId())
                            .content(comment.getContent())
                            .author(buildAuthorDto(comment.getUser()))
                            .createdAt(comment.getCreatedAt())
                            .replyCount(replyCount)
                            .replies(replyDtos)
                            .build();
                })
                .collect(Collectors.toList());

        return CommentListResponse.builder()
                .comments(commentResponses)
                .totalCount(commentResponses.size())
                .build();
    }

    /**
     * 댓글 작성
     */
    @Transactional
    public CommentCreateResponse createComment(Long postId, CommentCreateRequest request) {
        // Post 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 권한 검증
        validateProjectAccess(post.getStep().getProject().getId());

        // 현재 사용자 조회
        User user = getCurrentUser();

        // Comment 생성
        Comment comment = Comment.builder()
                .content(request.getContent())
                .post(post)
                .user(user)
                .parentComment(null)
                .build();

        comment = commentRepository.save(comment);

        return CommentCreateResponse.builder()
                .commentId(comment.getId())
                .build();
    }

    /**
     * 대댓글 페이징 조회
     */
    @Transactional(readOnly = true)
    public ReplyListResponse getReplies(Long commentId, int page, int size) {
        // 부모 댓글 조회
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // 권한 검증
        validateProjectAccess(parentComment.getPost().getStep().getProject().getId());

        // 대댓글 페이징 조회
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> repliesPage = commentRepository.findByParentCommentId(commentId, pageable);

        // ReplyDto로 변환
        List<ReplyListResponse.ReplyDto> replyDtos = repliesPage.getContent().stream()
                .map(reply -> ReplyListResponse.ReplyDto.builder()
                        .commentId(reply.getId())
                        .content(reply.getContent())
                        .author(ReplyListResponse.AuthorDto.builder()
                                .memberId(reply.getUser().getId())
                                .name(reply.getUser().getName())
                                .role(reply.getUser().getRole().name())
                                .companyName(reply.getUser().getCompany() != null
                                        ? reply.getUser().getCompany().getName()
                                        : null)
                                .build())
                        .createdAt(reply.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ReplyListResponse.builder()
                .replies(replyDtos)
                .totalCount((int) repliesPage.getTotalElements())
                .currentPage(page)
                .totalPages(repliesPage.getTotalPages())
                .hasNext(repliesPage.hasNext())
                .build();
    }

    /**
     * 대댓글 작성
     */
    @Transactional
    public CommentCreateResponse createReply(Long commentId, CommentCreateRequest request) {
        // 부모 댓글 조회
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // 권한 검증
        validateProjectAccess(parentComment.getPost().getStep().getProject().getId());

        // 현재 사용자 조회
        User user = getCurrentUser();

        // 대댓글 생성
        Comment reply = Comment.builder()
                .content(request.getContent())
                .post(parentComment.getPost())
                .user(user)
                .parentComment(parentComment)
                .build();

        reply = commentRepository.save(reply);

        return CommentCreateResponse.builder()
                .commentId(reply.getId())
                .build();
    }

    /**
     * 댓글 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteComment(Long commentId) {
        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // 작성자 권한 검증 (본인만 삭제 가능)
        User currentUser = getCurrentUser();
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 이미 삭제된 댓글인지 확인
        if (comment.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.COMMENT_ALREADY_DELETED);
        }

        // Soft Delete
        comment.softDelete();
    }

    /**
     * 프로젝트 접근 권한 검증
     */
    private void validateProjectAccess(Long projectId) {
        User currentUser = getCurrentUser();

        // SYSTEM_ADMIN은 모든 프로젝트 접근 가능
        if (currentUser.getRole() == UserRole.SYSTEM_ADMIN) {
            return;
        }

        // AGENCY, CLIENT는 프로젝트 멤버인 경우만 접근 가능
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUser.getId());
        if (!isMember) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    /**
     * 현재 사용자 조회
     */
    private User getCurrentUser() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        return userDetails.getUser();
    }

    /**
     * AuthorDto 빌드
     */
    private CommentResponse.AuthorDto buildAuthorDto(User user) {
        return CommentResponse.AuthorDto.builder()
                .memberId(user.getId())
                .name(user.getName())
                .role(user.getRole().name())
                .companyName(user.getCompany() != null ? user.getCompany().getName() : null)
                .build();
    }

}
