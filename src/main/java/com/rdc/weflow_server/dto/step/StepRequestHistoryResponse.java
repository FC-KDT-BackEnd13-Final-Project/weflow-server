package com.rdc.weflow_server.dto.step;

import com.rdc.weflow_server.entity.step.StepRequestHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class StepRequestHistoryResponse {

    private Long id;
    private StepRequestHistory.HistoryType historyType;
    private String fieldName;
    private String beforeContent;
    private String afterContent;
    private Long updatedBy;
    private LocalDateTime createdAt;
}
