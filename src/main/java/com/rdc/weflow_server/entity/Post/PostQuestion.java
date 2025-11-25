package com.rdc.weflow_server.entity.Post;

import com.rdc.weflow_server.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "post_questions")
public class PostQuestion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    // 질문 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;      // 게시글 ID

    @Column(columnDefinition = "TEXT", nullable = false)
    private String questionText;    // 질문 내용

    @Column(length = 50)
    private String confirmLabel;    // 확인 버튼 라벨

    @Column(length = 50)
    private String rejectLabel;     // 거절 버튼 라벨

}
