package com.rdc.weflow_server.entity.post;

import com.rdc.weflow_server.entity.BaseEntity;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import com.rdc.weflow_server.entity.step.Step;
import com.rdc.weflow_server.entity.user.User;
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
    private Long id; // 게시글 ID

    @Column(nullable = false)
    private String title; // 제목

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 내용

    @Column
    private ProjectStatus projectStatus; // 게시글이 어떤 phase에 속해있는지 계약-진행-납품-유지보수

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostApprovalStatus status; // NORMAL, WAITING_CONFIRM, CONFIRMED, REJECTED, DELETED

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostOpenStatus openStatus; // OPEN, CLOSED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_post_id")
    private Post parentPost; // 상위 게시글

    @OneToMany(mappedBy = "parentPost")
    @Builder.Default
    private List<Post> children = new ArrayList<>(); // 하위 게시글 리스트

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id")
    private Step step; // 단계 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 작성자 ID

    // 업데이트 메소드
    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateStatus(PostApprovalStatus status) {
        this.status = status;
    }

}
