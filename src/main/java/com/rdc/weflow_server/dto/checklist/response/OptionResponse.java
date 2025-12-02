package com.rdc.weflow_server.dto.checklist.response;

import com.rdc.weflow_server.entity.checklist.ChecklistOption;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OptionResponse {
    private Long optionId;
    private String optionText;
    private Boolean hasInput;
    private Integer orderIndex;
    public static OptionResponse from(ChecklistOption option) {
        return OptionResponse.builder()
                .optionId(option.getId())
                .optionText(option.getOptionText())
                .hasInput(option.getHasInput())
                .orderIndex(option.getOrderIndex())
                .build();
    }
}
