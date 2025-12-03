package com.rdc.weflow_server.controller.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rdc.weflow_server.config.WithCustomMockUser;
import com.rdc.weflow_server.dto.comment.CommentCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("Comment CRUD 통합 테스트")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // InitialDataLoader에서 생성된 데이터 기준
    private static final Long POST_ID = 1L;
    private static final Long COMMENT_ID = 1L;

    @Test
    @DisplayName("댓글 목록 조회")
    @WithCustomMockUser
    void getComments() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}/comments", POST_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("댓글 목록 조회 성공"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.comments").isArray());
    }

    @Test
    @DisplayName("댓글 작성")
    @WithCustomMockUser
    void createComment() throws Exception {
        CommentCreateRequest request = new CommentCreateRequest("테스트 댓글 내용입니다.");

        mockMvc.perform(post("/api/posts/{postId}/comments", POST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("댓글 작성 성공"))
                .andExpect(jsonPath("$.data.commentId").exists());
    }

    @Test
    @DisplayName("대댓글 목록 조회")
    @WithCustomMockUser
    void getReplies() throws Exception {
        mockMvc.perform(get("/api/comments/{commentId}/replies", COMMENT_ID)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("대댓글 조회 성공"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.replies").isArray());
    }

    @Test
    @DisplayName("대댓글 작성")
    @WithCustomMockUser
    void createReply() throws Exception {
        CommentCreateRequest request = new CommentCreateRequest("테스트 대댓글 내용입니다.");

        mockMvc.perform(post("/api/comments/{commentId}/replies", COMMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("대댓글 작성 성공"))
                .andExpect(jsonPath("$.data.commentId").exists());
    }

    @Test
    @DisplayName("댓글 삭제")
    @WithCustomMockUser
    void deleteComment() throws Exception {
        mockMvc.perform(delete("/api/comments/{commentId}", COMMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("댓글 삭제 성공"));
    }

    @Test
    @DisplayName("대댓글 목록 페이징 조회 - 두번째 페이지")
    @WithCustomMockUser
    void getRepliesWithPagination() throws Exception {
        mockMvc.perform(get("/api/comments/{commentId}/replies", COMMENT_ID)
                        .param("page", "1")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }
}
