package com.rdc.weflow_server.service.step;

import com.rdc.weflow_server.dto.step.StepRequestHistoryListResponse;
import com.rdc.weflow_server.dto.step.StepRequestHistoryResponse;
import com.rdc.weflow_server.entity.step.StepRequest;
import com.rdc.weflow_server.entity.step.StepRequestHistory;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.step.StepRequestHistoryRepository;
import com.rdc.weflow_server.repository.step.StepRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StepRequestHistoryService {

    private final StepRequestHistoryRepository stepRequestHistoryRepository;
    private final StepRequestRepository stepRequestRepository;

    public StepRequestHistoryListResponse getHistoriesByRequest(Long requestId, String sort) {
        // 요청 존재 여부만 선확인 (삭제된 Step라도 Request 히스토리는 그대로 조회 가능)
        stepRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STEP_REQUEST_NOT_FOUND));

        // TODO: 히스토리 조회는 "프로젝트 멤버(개발사/고객사 모두)"만 가능

        List<StepRequestHistory> histories;
        if (sort == null || "asc".equalsIgnoreCase(sort)) {
            histories = stepRequestHistoryRepository.findAscByRequestId(requestId);
        } else if ("desc".equalsIgnoreCase(sort)) {
            histories = stepRequestHistoryRepository.findDescByRequestId(requestId);
        } else {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        List<StepRequestHistoryResponse> responses = histories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return StepRequestHistoryListResponse.builder()
                .requestId(requestId)
                .histories(responses)
                .build();
    }

    private StepRequestHistoryResponse toResponse(StepRequestHistory history) {
        return StepRequestHistoryResponse.builder()
                .id(history.getId())
                .historyType(history.getHistoryType())
                .fieldName(history.getFieldName())
                .beforeContent(history.getBeforeContent())
                .afterContent(history.getAfterContent())
                .updatedBy(history.getUpdatedBy() != null ? history.getUpdatedBy().getId() : null)
                .createdAt(history.getCreatedAt())
                .build();
    }

    public StepRequestHistory saveFileUpdateHistory(StepRequest request, String beforeContent, String afterContent, User updatedBy) {
        // TODO: 첨부파일 변경 내역 구조 결정 후 before/after 포맷(JSON 등) 확정
        StepRequestHistory history = StepRequestHistory.builder()
                .request(request)
                .updatedBy(updatedBy)
                .historyType(StepRequestHistory.HistoryType.FILE_UPDATE)
                .fieldName("attachments")
                .beforeContent(beforeContent)
                .afterContent(afterContent)
                .build();
        return stepRequestHistoryRepository.save(history);
    }
}
