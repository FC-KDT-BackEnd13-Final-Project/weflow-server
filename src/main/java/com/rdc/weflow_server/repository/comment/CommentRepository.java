package com.rdc.weflow_server.repository.comment;

import com.rdc.weflow_server.entity.comment.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글의 최상위 댓글들 조회 (대댓글 제외, 삭제되지 않은 것만)
    @Query("SELECT c FROM Comment c " +
            "WHERE c.post.id = :postId " +
            "AND c.parentComment IS NULL " +
            "AND c.deletedAt IS NULL " +
            "ORDER BY c.createdAt ASC")
    List<Comment> findByPostIdAndParentCommentIsNull(@Param("postId") Long postId);

    // 특정 댓글의 대댓글들 조회 (페이징, 삭제되지 않은 것만)
    @Query("SELECT c FROM Comment c " +
            "WHERE c.parentComment.id = :parentCommentId " +
            "AND c.deletedAt IS NULL " +
            "ORDER BY c.createdAt ASC")
    Page<Comment> findByParentCommentId(@Param("parentCommentId") Long parentCommentId, Pageable pageable);

    // 특정 댓글의 대댓글들 조회 (리스트, 미리보기용)
    @Query("SELECT c FROM Comment c " +
            "WHERE c.parentComment.id = :parentCommentId " +
            "AND c.deletedAt IS NULL " +
            "ORDER BY c.createdAt ASC")
    List<Comment> findByParentCommentIdList(@Param("parentCommentId") Long parentCommentId);

    // 대댓글 개수 카운트 (삭제되지 않은 것만)
    @Query("SELECT COUNT(c) FROM Comment c " +
            "WHERE c.parentComment.id = :parentCommentId " +
            "AND c.deletedAt IS NULL")
    Integer countByParentCommentId(@Param("parentCommentId") Long parentCommentId);

    // 특정 게시글의 모든 댓글 조회 (삭제 처리용)
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.deletedAt IS NULL")
    List<Comment> findAllByPostId(@Param("postId") Long postId);

}
