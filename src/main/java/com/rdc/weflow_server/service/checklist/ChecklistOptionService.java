package com.rdc.weflow_server.service.checklist;

import com.rdc.weflow_server.dto.checklist.request.OptionReorderRequest;
import com.rdc.weflow_server.dto.checklist.request.OptionRequest;
import com.rdc.weflow_server.entity.checklist.ChecklistOption;
import com.rdc.weflow_server.entity.checklist.ChecklistQuestion;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.checklist.ChecklistOptionRepository;
import com.rdc.weflow_server.repository.checklist.ChecklistQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChecklistOptionService {
    private final ChecklistOptionRepository optionRepository;
    private final ChecklistQuestionRepository questionRepository;

    // 옵션 생성
    public Long createOption(OptionRequest request) {

        ChecklistQuestion question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_QUESTION_NOT_FOUND));

        int nextOrder = optionRepository.findAllByQuestionOrderByOrderIndexAsc(question).size() + 1;

        ChecklistOption option = ChecklistOption.builder()
                .question(question)
                .optionText(request.getOptionText())
                .hasInput(request.getHasInput())
                .orderIndex(nextOrder)
                .build();

        optionRepository.save(option);
        return option.getId();
    }

    // 옵션 수정
    public Long updateOption(Long optionId, OptionRequest request) {

        ChecklistOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_OPTION_NOT_FOUND));

        // 업데이트 가능한 필드만 부분 변경
        option.updateOption(request.getOptionText(), request.getHasInput(), null);

        return option.getId();
    }

    // 옵션 삭제
    public void deleteOption(Long optionId) {
        ChecklistOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_OPTION_NOT_FOUND));

        optionRepository.delete(option);
    }

    // 옵션 순서 재정렬
    @Transactional
    public void reorderOptions(OptionReorderRequest request) {

        questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_QUESTION_NOT_FOUND));

        List<Long> orderedIds = request.getOrderedIds();

        for (int i = 0; i < orderedIds.size(); i++) {
            Long optionId = orderedIds.get(i);

            ChecklistOption option = optionRepository.findById(optionId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_OPTION_NOT_FOUND));

            if (!option.getQuestion().getId().equals(request.getQuestionId())) {
                throw new BusinessException(ErrorCode.CHECKLIST_INVALID_OPTION_SEQUENCE);
            }

            option.updateOption(
                    option.getOptionText(),
                    option.getHasInput(),
                    i + 1
            );
        }
    }

}
