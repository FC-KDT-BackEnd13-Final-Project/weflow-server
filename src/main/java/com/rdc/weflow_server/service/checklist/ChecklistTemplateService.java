package com.rdc.weflow_server.service.checklist;

import com.rdc.weflow_server.dto.checklist.response.TemplateDetailResponse;
import com.rdc.weflow_server.dto.checklist.response.TemplateResponse;
import com.rdc.weflow_server.dto.checklist.request.TemplateRequest;
import com.rdc.weflow_server.entity.checklist.Checklist;
import com.rdc.weflow_server.entity.checklist.ChecklistQuestion;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.checklist.ChecklistQuestionRepository;
import com.rdc.weflow_server.repository.checklist.ChecklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChecklistTemplateService {
    private final ChecklistRepository checklistRepository;
    private final ChecklistQuestionRepository checklistQuestionRepository;

    // 템플릿 생성
    public Long createTemplate(TemplateRequest request) {
        Checklist template = Checklist.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .isTemplate(true)
                .isLocked(false)
                .step(null)
                .template(null)
                .build();

        checklistRepository.save(template);
        return template.getId();
    }

    // 템플릿 목록 조회
    @Transactional(readOnly = true)
    public List<TemplateResponse> getTemplateList() {

        List<Checklist> templates = checklistRepository.findByIsTemplateTrue();

        return templates.stream()
                .map(template -> {
                    int questionCount = checklistQuestionRepository.countByChecklist(template);
                    return TemplateResponse.from(template, questionCount);
                })
                .toList();
    }

    // 템플릿 상세 조회
    @Transactional(readOnly = true)
    public TemplateDetailResponse getTemplateDetail(Long templateId) {

        Checklist template = checklistRepository.findByIdAndIsTemplateTrue(templateId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));

        // 질문 + 옵션 조회
        List<ChecklistQuestion> questions =
                checklistQuestionRepository.findAllByChecklistOrderByOrderIndexAsc(template);

        return TemplateDetailResponse.from(template, questions);
    }

    // 템플릿 수정
    public Long updateTemplate(Long templateId, TemplateRequest request) {

        Checklist template = checklistRepository.findByIdAndIsTemplateTrue(templateId)
                .orElseThrow(() -> new RuntimeException("TEMPLATE_NOT_FOUND"));

        template.updateTemplate(
                request.getTitle(),
                request.getDescription(),
                request.getCategory()
        );

        return template.getId();
    }

    // 템플릿 삭제
    public Long deleteTemplate(Long templateId) {
        Checklist template = checklistRepository.findByIdAndIsTemplateTrue(templateId)
                .orElseThrow(() -> new RuntimeException("TEMPLATE_NOT_FOUND"));

        checklistRepository.delete(template);
        return template.getId();
    }
}
