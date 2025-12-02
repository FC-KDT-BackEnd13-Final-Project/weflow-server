package com.rdc.weflow_server.repository.checklist;

import com.rdc.weflow_server.entity.checklist.ChecklistOption;
import com.rdc.weflow_server.entity.checklist.ChecklistQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChecklistOptionRepository extends JpaRepository<ChecklistOption, Long> {
    List<ChecklistOption> findAllByQuestionOrderByOrderIndexAsc(ChecklistQuestion question);
}
