package com.rdc.weflow_server.entity.Comment;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "comment_histories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "version"}))
public class CommentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    // 댓글 이력 ID

    @Column(nullable = false)
    private Integer version;    // 버전

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;     // 내용

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;    // 수정시각

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;    // 댓글 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", nullable = false)
    private User user;      // 작성자 ID

}
