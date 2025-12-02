package com.rdc.weflow_server.repository.post;

import com.rdc.weflow_server.entity.post.PostAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostAnswerRepository extends JpaRepository<PostAnswer, Long> {

    Optional<PostAnswer> findByQuestionId(Long questionId);
}
