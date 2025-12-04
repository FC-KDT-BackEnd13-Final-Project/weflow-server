package com.rdc.weflow_server.dto.post;

import com.rdc.weflow_server.entity.post.AnswerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostAnswerRequest {

    private AnswerType answerType;
    private String content;  // 사유/의견 (선택사항)

}
