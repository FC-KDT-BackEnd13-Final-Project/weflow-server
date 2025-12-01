package com.rdc.weflow_server.service.post;

import com.rdc.weflow_server.dto.post.PostDetailResponse;
import com.rdc.weflow_server.dto.post.PostListResponse;
import com.rdc.weflow_server.entity.attachment.Attachment;
import com.rdc.weflow_server.entity.post.Post;
import com.rdc.weflow_server.entity.post.PostQuestion;
import com.rdc.weflow_server.entity.step.Phase;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.attachment.AttachmentRepository;
import com.rdc.weflow_server.repository.post.PostQuestionRepository;
import com.rdc.weflow_server.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final AttachmentRepository attachmentRepository;
    private final PostQuestionRepository postQuestionRepository;

    /**
     * 게시글 상세 조회
     * TODO: 나중에 fetch join 등으로 최적화 필요.
     */
    public PostDetailResponse getPost(Long projectId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // projectId 검증: 해당 게시글이 요청한 프로젝트에 속하는지 확인
        if (!post.getStep().getProject().getId().equals(projectId)) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        // 첨부파일 및 링크 조회
        List<Attachment> allAttachments = attachmentRepository
                .findByTargetTypeAndTargetId(Attachment.TargetType.POST, postId);

        // FILE 타입 필터링
        List<PostDetailResponse.AttachmentDto> attachments = allAttachments.stream()
                .filter(a -> a.getAttachmentType() == Attachment.AttachmentType.FILE)
                .map(a -> PostDetailResponse.AttachmentDto.builder()
                        .id(a.getId())
                        .fileName(a.getFileName())
                        .fileSize(a.getFileSize())
                        .filePath(a.getFilePath())
                        .build())
                .toList();

        // LINK 타입 필터링
        List<PostDetailResponse.LinkDto> links = allAttachments.stream()
                .filter(a -> a.getAttachmentType() == Attachment.AttachmentType.LINK)
                .map(a -> PostDetailResponse.LinkDto.builder()
                        .id(a.getId())
                        .url(a.getUrl())
                        .build())
                .toList();

        // 질문 조회
        List<PostQuestion> postQuestions = postQuestionRepository.findByPostId(postId);
        List<PostDetailResponse.QuestionDto> questions = postQuestions.stream()
                .map(q -> PostDetailResponse.QuestionDto.builder()
                        .id(q.getId())
                        .questionText(q.getQuestionText())
                        .confirmLabel(q.getConfirmLabel())
                        .rejectLabel(q.getRejectLabel())
                        .build())
                .toList();

        // Response 생성
        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(PostDetailResponse.AuthorDto.builder()
                        .id(post.getUser().getId())
                        .name(post.getUser().getName())
                        .email(post.getUser().getEmail())
                        .build())
                .createdAt(post.getCreatedDate())
                .updatedAt(post.getLastModifiedDate())
                .attachments(attachments)
                .links(links)
                .questions(questions)
                .build();
    }

    /**
     * 게시글 list 조회
     * TODO: 나중에 동적 쿼리 (Querydsl 등으로 refactoring 할 필요 있음)
     */
    public List<PostListResponse> getPosts(Long projectId, Phase phase, Long stepId) {
        List<Post> posts;

        // 필터에 따라 게시글 조회
        if (stepId != null) {
            posts = postRepository.findByStepId(stepId);
        } else if (phase != null) {
            posts = postRepository.findByStepProjectIdAndStepPhase(projectId, phase);
        } else {
            posts = postRepository.findByStepProjectId(projectId);
        }

        // PostListResponse로 변환
        return posts.stream()
                .map(post -> {
                    // 파일 갯수 조회
                    int fileCount = attachmentRepository.countByTargetTypeAndTargetIdAndAttachmentType(
                            Attachment.TargetType.POST,
                            post.getId(),
                            Attachment.AttachmentType.FILE
                    );

                    // 댓글 갯수 (children 리스트 크기)
                    int commentCount = post.getChildren().size();

                    return PostListResponse.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .author(PostDetailResponse.AuthorDto.builder()
                                    .id(post.getUser().getId())
                                    .name(post.getUser().getName())
                                    .email(post.getUser().getEmail())
                                    .build())
                            .createdAt(post.getCreatedDate())
                            .openStatus(post.getOpenStatus())
                            .status(post.getStatus())
                            .fileCount(fileCount)
                            .commentCount(commentCount)
                            .build();
                })
                .toList();
    }

    // 게시글 작성


    // 게시글 수정

    // 게시글 삭제
}
