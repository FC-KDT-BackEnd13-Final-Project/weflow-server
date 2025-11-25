package com.rdc.weflow_server.entity.Post;

import com.rdc.weflow_server.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "posts")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;            // 게시글 ID

    @Column(nullable = false, length = 255)
    private String title;       // 제목

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;     // 내용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status;  // 상태 (ENUM)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_post_id")
    private Post parentPost;    // 상위 게시글

    @OneToMany(mappedBy = "parentPost")
    @Builder.Default
    private List<Post> children = new ArrayList<>();    // 하위 게시글 리스트

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id")
    private Step step;   // 단계 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;      // 작성자 ID

}
