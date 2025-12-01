package com.rdc.weflow_server.repository.step;

import com.rdc.weflow_server.entity.step.Step;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StepRepository extends JpaRepository<Step, Long> {
}
