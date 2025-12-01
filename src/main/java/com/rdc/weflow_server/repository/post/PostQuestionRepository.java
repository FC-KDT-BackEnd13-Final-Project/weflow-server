package com.rdc.weflow_server.repository.post;

import com.rdc.weflow_server.entity.post.PostQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostQuestionRepository extends JpaRepository<PostQuestion, Long> {

    List<PostQuestion> findByPostId(Long postId);

    void deleteByPostId(Long postId);
}
