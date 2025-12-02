package com.rdc.weflow_server.repository.checklist;

import com.rdc.weflow_server.entity.checklist.Checklist;
import com.rdc.weflow_server.entity.checklist.ChecklistQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChecklistQuestionRepository extends JpaRepository<ChecklistQuestion, Long> {
    int countByChecklist(Checklist checklist); // 질문 수
    List<ChecklistQuestion> findAllByChecklistOrderByOrderIndexAsc(Checklist checklist); // 질문 목록
}
