package com.rdc.weflow_server.controller.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rdc.weflow_server.config.WithCustomMockUser;
import com.rdc.weflow_server.dto.post.PostCreateRequest;
import com.rdc.weflow_server.dto.post.PostUpdateRequest;
import com.rdc.weflow_server.entity.project.ProjectStatus;
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
@DisplayName("Post CRUD 통합 테스트")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // InitialDataLoader에서 생성된 데이터 기준
    private static final Long PROJECT_ID = 1L;
    private static final Long POST_ID = 1L;
    private static final Long STEP_ID = 2L;

    @Test
    @DisplayName("게시글 목록 조회")
    @WithCustomMockUser
    void getPosts() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/posts", PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시글 목록 조회 성공"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("게시글 상세 조회")
    @WithCustomMockUser
    void getPost() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/posts/{postId}", PROJECT_ID, POST_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시글 조회 성공"))
                .andExpect(jsonPath("$.data.postId").value(POST_ID))
                .andExpect(jsonPath("$.data.title").exists())
                .andExpect(jsonPath("$.data.content").exists());
    }

    @Test
    @DisplayName("게시글 작성")
    @WithCustomMockUser
    void createPost() throws Exception {
        PostCreateRequest request = new PostCreateRequest(
                "테스트 게시글 제목",
                "테스트 게시글 내용입니다.",
                STEP_ID,
                null,
                ProjectStatus.IN_PROGRESS,
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/projects/{projectId}/posts", PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시글 작성 성공"))
                .andExpect(jsonPath("$.data.postId").exists());
    }

    @Test
    @DisplayName("게시글 수정")
    @WithCustomMockUser
    void updatePost() throws Exception {
        PostUpdateRequest request = new PostUpdateRequest(
                "수정된 게시글 제목",
                "수정된 게시글 내용입니다.",
                null,
                null,
                null
        );

        mockMvc.perform(patch("/api/projects/{projectId}/posts/{postId}", PROJECT_ID, POST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시글 수정 성공"))
                .andExpect(jsonPath("$.data.postId").value(POST_ID))
                .andExpect(jsonPath("$.data.title").value("수정된 게시글 제목"))
                .andExpect(jsonPath("$.data.content").value("수정된 게시글 내용입니다."));
    }

    @Test
    @DisplayName("게시글 삭제")
    @WithCustomMockUser
    void deletePost() throws Exception {
        mockMvc.perform(delete("/api/projects/{projectId}/posts/{postId}", PROJECT_ID, POST_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시글 삭제 성공"));
    }

    @Test
    @DisplayName("게시글 목록 조회 - projectStatus 필터")
    @WithCustomMockUser
    void getPostsWithProjectStatusFilter() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/posts", PROJECT_ID)
                        .param("projectStatus", "IN_PROGRESS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("게시글 목록 조회 - stepId 필터")
    @WithCustomMockUser
    void getPostsWithStepIdFilter() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/posts", PROJECT_ID)
                        .param("stepId", String.valueOf(STEP_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }
}
