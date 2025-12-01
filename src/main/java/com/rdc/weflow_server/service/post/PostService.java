package com.rdc.weflow_server.service.post;

import com.rdc.weflow_server.dto.post.PostCreateRequest;
import com.rdc.weflow_server.dto.post.PostDetailResponse;
import com.rdc.weflow_server.dto.post.PostListResponse;
import com.rdc.weflow_server.dto.post.PostUpdateRequest;
import com.rdc.weflow_server.entity.attachment.Attachment;
import com.rdc.weflow_server.entity.post.Post;
import com.rdc.weflow_server.entity.post.PostApprovalStatus;
import com.rdc.weflow_server.entity.post.PostOpenStatus;
import com.rdc.weflow_server.entity.post.PostQuestion;
import com.rdc.weflow_server.entity.step.Phase;
import com.rdc.weflow_server.entity.step.Step;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.attachment.AttachmentRepository;
import com.rdc.weflow_server.repository.post.PostQuestionRepository;
import com.rdc.weflow_server.repository.post.PostRepository;
import com.rdc.weflow_server.repository.step.StepRepository;
import com.rdc.weflow_server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final AttachmentRepository attachmentRepository;
    private final PostQuestionRepository postQuestionRepository;
    private final StepRepository stepRepository;
    private final UserRepository userRepository;

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

    /**
     * 게시글 작성
     */
    @Transactional
    public PostDetailResponse createPost(Long projectId, PostCreateRequest request) {
        // Step 조회 및 검증
        Step step = stepRepository.findById(request.getStepId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STEP_NOT_FOUND));

        // Step이 해당 프로젝트에 속하는지 검증
        if (!step.getProject().getId().equals(projectId)) {
            throw new BusinessException(ErrorCode.STEP_NOT_FOUND);
        }

        // User 조회
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // ParentPost 조회 (답글인 경우)
        Post parentPost = null;
        if (request.getParentPostId() != null) {
            parentPost = postRepository.findById(request.getParentPostId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        }

        // 질문이 있으면 WAITING_CONFIRM, 없으면 NORMAL
        PostApprovalStatus status = (request.getQuestions() != null && !request.getQuestions().isEmpty())
                ? PostApprovalStatus.WAITING_CONFIRM
                : PostApprovalStatus.NORMAL;

        // Post 생성 및 저장
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .status(status)
                .openStatus(PostOpenStatus.OPEN)
                .step(step)
                .user(user)
                .parentPost(parentPost)
                .build();
        post = postRepository.save(post);

        // Attachments 저장 (FILE)
        if (request.getAttachments() != null) {
            for (PostCreateRequest.AttachmentRequest attachmentReq : request.getAttachments()) {
                Attachment attachment = Attachment.builder()
                        .targetType(Attachment.TargetType.POST)
                        .targetId(post.getId())
                        .attachmentType(Attachment.AttachmentType.FILE)
                        .fileName(attachmentReq.getFileName())
                        .fileSize(attachmentReq.getFileSize())
                        .filePath(attachmentReq.getFilePath())
                        .build();
                attachmentRepository.save(attachment);
            }
        }

        // Links 저장 (LINK)
        if (request.getLinks() != null) {
            for (PostCreateRequest.LinkRequest linkReq : request.getLinks()) {
                Attachment link = Attachment.builder()
                        .targetType(Attachment.TargetType.POST)
                        .targetId(post.getId())
                        .attachmentType(Attachment.AttachmentType.LINK)
                        .url(linkReq.getUrl())
                        .build();
                attachmentRepository.save(link);
            }
        }

        // Questions 저장
        if (request.getQuestions() != null) {
            for (PostCreateRequest.QuestionRequest questionReq : request.getQuestions()) {
                PostQuestion question = PostQuestion.builder()
                        .post(post)
                        .questionText(questionReq.getQuestionText())
                        .confirmLabel(questionReq.getConfirmLabel())
                        .rejectLabel(questionReq.getRejectLabel())
                        .build();
                postQuestionRepository.save(question);
            }
        }

        // 생성된 게시글 상세 정보 반환
        return getPost(projectId, post.getId());
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public PostDetailResponse updatePost(Long projectId, Long postId, PostUpdateRequest request) {
        // Post 조회 및 검증
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // projectId 검증
        if (!post.getStep().getProject().getId().equals(projectId)) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        // 제목 및 내용 수정
        if (request.getTitle() != null) {
            post.updateTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            post.updateContent(request.getContent());
        }

        // 기존 첨부파일 삭제
        if (request.getAttachments() != null) {
            attachmentRepository.deleteByTargetTypeAndTargetIdAndAttachmentType(
                    Attachment.TargetType.POST,
                    postId,
                    Attachment.AttachmentType.FILE
            );

            // 새 첨부파일 저장
            for (PostUpdateRequest.AttachmentRequest attachmentReq : request.getAttachments()) {
                Attachment attachment = Attachment.builder()
                        .targetType(Attachment.TargetType.POST)
                        .targetId(postId)
                        .attachmentType(Attachment.AttachmentType.FILE)
                        .fileName(attachmentReq.getFileName())
                        .fileSize(attachmentReq.getFileSize())
                        .filePath(attachmentReq.getFilePath())
                        .build();
                attachmentRepository.save(attachment);
            }
        }

        // 기존 링크 삭제
        if (request.getLinks() != null) {
            attachmentRepository.deleteByTargetTypeAndTargetIdAndAttachmentType(
                    Attachment.TargetType.POST,
                    postId,
                    Attachment.AttachmentType.LINK
            );

            // 새 링크 저장
            for (PostUpdateRequest.LinkRequest linkReq : request.getLinks()) {
                Attachment link = Attachment.builder()
                        .targetType(Attachment.TargetType.POST)
                        .targetId(postId)
                        .attachmentType(Attachment.AttachmentType.LINK)
                        .url(linkReq.getUrl())
                        .build();
                attachmentRepository.save(link);
            }
        }

        // 기존 질문 삭제
        if (request.getQuestions() != null) {
            postQuestionRepository.deleteByPostId(postId);

            // 새 질문 저장
            for (PostUpdateRequest.QuestionRequest questionReq : request.getQuestions()) {
                PostQuestion question = PostQuestion.builder()
                        .post(post)
                        .questionText(questionReq.getQuestionText())
                        .confirmLabel(questionReq.getConfirmLabel())
                        .rejectLabel(questionReq.getRejectLabel())
                        .build();
                postQuestionRepository.save(question);
            }

            // 질문 유무에 따라 상태 업데이트
            PostApprovalStatus newStatus = request.getQuestions().isEmpty()
                    ? PostApprovalStatus.NORMAL
                    : PostApprovalStatus.WAITING_CONFIRM;
            post.updateStatus(newStatus);
        }

        // 수정된 게시글 상세 정보 반환
        return getPost(projectId, postId);
    }

    // 게시글 삭제
}
