package com.rdc.weflow_server.entity.project;

import com.rdc.weflow_server.entity.BaseEntity;
import com.rdc.weflow_server.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Table(name = "project_members")
@Entity
public class ProjectMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ProjectRole role;

    private LocalDateTime deletedAt;

    public static ProjectMember create(Project project, User user, ProjectRole role) {
        ProjectMember pm = new ProjectMember();
        pm.project = project;
        pm.user = user;
        pm.role = role;
        return pm;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
