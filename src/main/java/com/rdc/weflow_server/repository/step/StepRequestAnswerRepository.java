package com.rdc.weflow_server.repository.step;

import com.rdc.weflow_server.entity.step.StepRequestAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StepRequestAnswerRepository extends JpaRepository<StepRequestAnswer, Long> {

    Optional<StepRequestAnswer> findByStepRequest_Id(Long requestId);
}
