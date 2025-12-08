package com.rdc.weflow_server.dto.checklist.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OptionReorderRequest {
    private Long questionId;
    private List<Long> orderedIds; // 새로운 옵션 순서 ex) orderedIds = [3, 1, 5, 2]
}
