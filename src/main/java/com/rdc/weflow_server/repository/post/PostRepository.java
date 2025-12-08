package com.rdc.weflow_server.repository.post;

import com.rdc.weflow_server.entity.post.Post;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 단일 게시글 조회 (삭제되지 않은 것만)
    Optional<Post> findByIdAndDeletedAtIsNull(Long id);

    // 프로젝트 전체 게시글 조회
    List<Post> findByStepProjectIdAndDeletedAtIsNull(Long projectId);

    // 특정 projectStatus의 게시글 조회
    List<Post> findByStepProjectIdAndStepProjectStatusAndDeletedAtIsNull(Long projectId, ProjectStatus projectStatus);

    // 특정 step의 게시글 조회
    List<Post> findByStepIdAndDeletedAtIsNull(Long stepId);

    boolean existsByStepIdAndDeletedAtIsNull(Long stepId);

    // 페이지네이션 지원 메서드
    Page<Post> findByStepProjectIdAndDeletedAtIsNull(Long projectId, Pageable pageable);

    Page<Post> findByStepProjectIdAndStepProjectStatusAndDeletedAtIsNull(Long projectId, ProjectStatus projectStatus, Pageable pageable);

    Page<Post> findByStepIdAndDeletedAtIsNull(Long stepId, Pageable pageable);
}
