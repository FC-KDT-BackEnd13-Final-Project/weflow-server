package com.rdc.weflow_server.entity.Comment;

import com.rdc.weflow_server.entity.BaseEntity;
import com.rdc.weflow_server.entity.Post.Post;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comments")
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    // 댓글 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;  // 상위 댓글 ID

    @OneToMany(mappedBy = "parentComment")
    @Builder.Default
    private List<Comment> children = new ArrayList<>();

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;     // 내용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;  // 게시글 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 작성자 ID

}
