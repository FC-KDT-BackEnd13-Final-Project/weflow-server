package com.rdc.weflow_server.controller.checklist;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.dto.checklist.request.OptionReorderRequest;
import com.rdc.weflow_server.dto.checklist.request.OptionRequest;
import com.rdc.weflow_server.dto.checklist.response.OptionResponse;
import com.rdc.weflow_server.service.checklist.ChecklistOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/options")
@RequiredArgsConstructor
public class OptionController {
    private final ChecklistOptionService optionService;

    // 옵션 생성
    @PostMapping
    public ApiResponse<OptionResponse> createOption(
            @RequestBody OptionRequest request
    ) {
        Long optionId = optionService.createOption(request);

        return ApiResponse.success(
                "OPTION_CREATED",
                OptionResponse.builder()
                        .optionId(optionId)
                        .optionText(request.getOptionText())
                        .hasInput(request.getHasInput())
                        .build()
        );
    }

    // 옵션 수정
    @PatchMapping("/{optionId}")
    public ApiResponse<OptionResponse> updateOption(
            @PathVariable Long optionId,
            @RequestBody OptionRequest request
    ) {
        Long id = optionService.updateOption(optionId, request);

        return ApiResponse.success(
                "OPTION_UPDATED",
                OptionResponse.builder()
                        .optionId(id)
                        .optionText(request.getOptionText())
                        .hasInput(request.getHasInput())
                        .build()
        );
    }

    // 옵션 삭제
    @DeleteMapping("/{optionId}")
    public ApiResponse<Long> deleteOption(@PathVariable Long optionId) {

        optionService.deleteOption(optionId);

        return ApiResponse.success(
                "OPTION_DELETED",
                optionId
        );
    }

    // 옵션 순서 재정렬
    @PatchMapping("/reorder")
    public ApiResponse<Void> reorderOptions(
            @RequestBody OptionReorderRequest request
    ) {
        optionService.reorderOptions(request);
        return ApiResponse.success("OPTIONS_REORDERED", null);
    }

}
