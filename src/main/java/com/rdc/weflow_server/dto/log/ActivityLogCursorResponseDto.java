package com.rdc.weflow_server.dto.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogCursorResponseDto {
    private List<ActivityLogResponseDto> items;
    private CursorDto nextCursor;
    private boolean hasNext;
    private Long totalCount; // includeTotal=true일 때만 세팅, 아니면 null
}
