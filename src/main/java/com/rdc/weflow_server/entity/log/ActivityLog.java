package com.rdc.weflow_server.entity.log;

import com.rdc.weflow_server.entity.BaseEntity;
import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Table(name = "activity_logs")
@Entity
public class ActivityLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @Column
    @Enumerated(EnumType.STRING)
    private TargetTable targetTable;

    @Column
    private Long targetId;

    @Column
    private String ipAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

}
