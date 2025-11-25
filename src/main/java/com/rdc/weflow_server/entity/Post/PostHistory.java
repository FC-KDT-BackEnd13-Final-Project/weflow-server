package com.rdc.weflow_server.entity.Post;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "post_history",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "version"}))
public class PostHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    // 게시글 이력 ID

    @Column(nullable = false)
    private Integer version;     // 버전

    @Column(length = 255, nullable = false)
    private String title;   // 제목

    @Column(columnDefinition = "JSON")
    private String attachments;     // 첨부파일

    @Column(columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;    // 수정시각

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;  // 게시글 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", nullable = false)
    private User user;  // 작성자 ID

}
