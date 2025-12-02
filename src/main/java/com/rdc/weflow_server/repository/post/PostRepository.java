package com.rdc.weflow_server.repository.post;

import com.rdc.weflow_server.entity.post.Post;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 프로젝트 전체 게시글 조회
    List<Post> findByStepProjectId(Long projectId);

    // 특정 projectStatus의 게시글 조회
    List<Post> findByStepProjectIdAndStepProjectStatus(Long projectId, ProjectStatus projectStatus);

    // 특정 step의 게시글 조회
    List<Post> findByStepId(Long stepId);
}
