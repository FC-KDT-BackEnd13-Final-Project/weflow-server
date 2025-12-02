package com.rdc.weflow_server.service.post;

import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.post.PostCreateRequest;
import com.rdc.weflow_server.dto.post.PostCreateResponse;
import com.rdc.weflow_server.dto.post.PostDetailResponse;
import com.rdc.weflow_server.dto.post.PostListResponse;
import com.rdc.weflow_server.dto.post.PostUpdateRequest;
import com.rdc.weflow_server.entity.attachment.Attachment;
import com.rdc.weflow_server.entity.post.Post;
import com.rdc.weflow_server.entity.post.PostApprovalStatus;
import com.rdc.weflow_server.entity.post.PostOpenStatus;
import com.rdc.weflow_server.entity.post.PostQuestion;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import com.rdc.weflow_server.entity.step.Step;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.attachment.AttachmentRepository;
import com.rdc.weflow_server.repository.post.PostAnswerRepository;
import com.rdc.weflow_server.repository.post.PostQuestionRepository;
import com.rdc.weflow_server.repository.post.PostRepository;
import com.rdc.weflow_server.repository.step.StepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final AttachmentRepository attachmentRepository;
    private final PostQuestionRepository postQuestionRepository;
    private final PostAnswerRepository postAnswerRepository;
    private final StepRepository stepRepository;

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
        List<PostDetailResponse.FileDto> files = allAttachments.stream()
                .filter(a -> a.getAttachmentType() == Attachment.AttachmentType.FILE)
                .map(a -> PostDetailResponse.FileDto.builder()
                        .fileId(a.getId())
                        .fileName(a.getFileName())
                        .fileSize(a.getFileSize())
                        .downloadUrl("/api/files/" + a.getId() + "/download")
                        .build())
                .toList();

        // LINK 타입 필터링
        List<PostDetailResponse.LinkDto> links = allAttachments.stream()
                .filter(a -> a.getAttachmentType() == Attachment.AttachmentType.LINK)
                .map(a -> PostDetailResponse.LinkDto.builder()
                        .linkId(a.getId())
                        .url(a.getUrl())
                        .title(null)  // Attachment 엔티티에 title 필드 없음
                        .build())
                .toList();

        // 질문 조회
        List<PostQuestion> postQuestions = postQuestionRepository.findByPostId(postId);
        List<PostDetailResponse.QuestionDto> questions = postQuestions.stream()
                .map(q -> {
                    // 질문에 대한 답변 조회
                    PostDetailResponse.AnswerDto answerDto = postAnswerRepository
                            .findByQuestionId(q.getId())
                            .map(answer -> PostDetailResponse.AnswerDto.builder()
                                    .response(answer.getAnswerType().name())
                                    .respondent(PostDetailResponse.RespondentDto.builder()
                                            .memberId(answer.getUser().getId())
                                            .name(answer.getUser().getName())
                                            .build())
                                    .respondedAt(answer.getCreatedAt())
                                    .build())
                            .orElse(null);

                    return PostDetailResponse.QuestionDto.builder()
                            .questionId(q.getId())
                            .content(q.getQuestionText())
                            .buttonLabels(PostDetailResponse.ButtonLabelsDto.builder()
                                    .yes(q.getConfirmLabel())
                                    .no(q.getRejectLabel())
                                    .build())
                            .answer(answerDto)
                            .build();
                })
                .toList();

        // 부모 게시글 정보
        PostDetailResponse.ParentPostDto parentPostDto = null;
        if (post.getParentPost() != null) {
            Post parent = post.getParentPost();
            String parentCompanyName = parent.getUser().getCompany() != null
                    ? parent.getUser().getCompany().getName()
                    : null;

            parentPostDto = PostDetailResponse.ParentPostDto.builder()
                    .postId(parent.getId())
                    .title(parent.getTitle())
                    .author(PostDetailResponse.AuthorDto.builder()
                            .memberId(parent.getUser().getId())
                            .name(parent.getUser().getName())
                            .role(parent.getUser().getRole().name())
                            .companyName(parentCompanyName)
                            .build())
                    .build();
        }

        // 작성자 정보
        String companyName = post.getUser().getCompany() != null
                ? post.getUser().getCompany().getName()
                : null;

        // 수정 여부
        boolean isEdited = !post.getCreatedAt().equals(post.getUpdatedAt());

        // Response 생성
        return PostDetailResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .status(post.getStatus())
                .author(PostDetailResponse.AuthorDto.builder()
                        .memberId(post.getUser().getId())
                        .name(post.getUser().getName())
                        .role(post.getUser().getRole().name())
                        .companyName(companyName)
                        .build())
                .projectStatus(post.getStep().getProject().getStatus())
                .step(PostDetailResponse.StepDto.builder()
                        .stepId(post.getStep().getId())
                        .stepName(post.getStep().getTitle())
                        .build())
                .files(files)
                .links(links)
                .questions(questions)
                .parentPost(parentPostDto)
                .isEdited(isEdited)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    /**
     * 게시글 list 조회
     * TODO: 나중에 동적 쿼리 (Querydsl 등으로 refactoring 할 필요 있음)
     */
    public PostListResponse getPosts(Long projectId, ProjectStatus projectStatus, Long stepId) {
        List<Post> posts;

        // 필터에 따라 게시글 조회
        if (stepId != null) {
            posts = postRepository.findByStepId(stepId);
        } else if (projectStatus != null) {
            posts = postRepository.findByStepProjectIdAndStepProjectStatus(projectId, projectStatus);
        } else {
            posts = postRepository.findByStepProjectId(projectId);
        }

        // PostListResponse로 변환
        List<PostListResponse.PostItem> postItems = posts.stream()
                .map(post -> {
                    // 파일 존재 여부
                    boolean hasFiles = attachmentRepository.countByTargetTypeAndTargetIdAndAttachmentType(
                            Attachment.TargetType.POST,
                            post.getId(),
                            Attachment.AttachmentType.FILE
                    ) > 0;

                    // 링크 존재 여부
                    boolean hasLinks = attachmentRepository.countByTargetTypeAndTargetIdAndAttachmentType(
                            Attachment.TargetType.POST,
                            post.getId(),
                            Attachment.AttachmentType.LINK
                    ) > 0;

                    // 질문 존재 여부
                    boolean hasQuestions = postQuestionRepository.findByPostId(post.getId()).size() > 0;

                    // 댓글 갯수 (children 리스트에서 parentPost가 있는 것만)
                    int commentCount = post.getChildren().size();

                    // 답글 갯수 (children의 children)
                    int replyCount = post.getChildren().stream()
                            .mapToInt(child -> child.getChildren().size())
                            .sum();

                    // 수정 여부 (생성일과 수정일이 다르면 수정됨)
                    boolean isEdited = !post.getCreatedAt().equals(post.getUpdatedAt());

                    // 작성자 정보
                    String companyName = post.getUser().getCompany() != null
                            ? post.getUser().getCompany().getName()
                            : null;

                    return PostListResponse.PostItem.builder()
                            .postId(post.getId())
                            .title(post.getTitle())
                            .status(post.getStatus())
                            .projectStatus(post.getStep().getProject().getStatus())
                            .stepId(post.getStep().getId())
                            .author(PostListResponse.AuthorDto.builder()
                                    .memberId(post.getUser().getId())
                                    .name(post.getUser().getName())
                                    .role(post.getUser().getRole().name())
                                    .companyName(companyName)
                                    .build())
                            .hasFiles(hasFiles)
                            .hasLinks(hasLinks)
                            .hasQuestions(hasQuestions)
                            .commentCount(commentCount)
                            .replyCount(replyCount)
                            .isEdited(isEdited)
                            .createdAt(post.getCreatedAt())
                            .updatedAt(post.getUpdatedAt())
                            .build();
                })
                .toList();

        return PostListResponse.builder()
                .posts(postItems)
                .build();
    }

    /**
     * 게시글 작성
     */
    @Transactional
    public PostCreateResponse createPost(Long projectId, PostCreateRequest request) {
        // Step 조회 및 검증
        Step step = stepRepository.findById(request.getStepId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STEP_NOT_FOUND));

        // Step이 해당 프로젝트에 속하는지 검증
        if (!step.getProject().getId().equals(projectId)) {
            throw new BusinessException(ErrorCode.STEP_NOT_FOUND);
        }

        // JWT에서 현재 인증된 사용자 정보 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        User user = userDetails.getUser();

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
                .projectStatus(request.getProjectStatus())
                .step(step)
                .user(user)
                .parentPost(parentPost)
                .build();
        post = postRepository.save(post);

        // Files 저장 (FILE)
        if (request.getFiles() != null) {
            for (PostCreateRequest.FileRequest fileReq : request.getFiles()) {
                Attachment attachment = Attachment.builder()
                        .targetType(Attachment.TargetType.POST)
                        .targetId(post.getId())
                        .attachmentType(Attachment.AttachmentType.FILE)
                        .fileName(fileReq.getFileName())
                        .fileSize(fileReq.getFileSize())
                        .filePath(fileReq.getFilePath())
                        .contentType(fileReq.getContentType())
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

        // 생성된 게시글 ID 반환
        return PostCreateResponse.builder()
                .postId(post.getId())
                .build();
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public PostDetailResponse updatePost(Long projectId, Long postId, PostUpdateRequest request) {
        // Post 조회 및 검증
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 작성자 권한 검증
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        if (!post.getUser().getId().equals(userDetails.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

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

        // 기존 파일 삭제
        if (request.getFiles() != null) {
            attachmentRepository.deleteByTargetTypeAndTargetIdAndAttachmentType(
                    Attachment.TargetType.POST,
                    postId,
                    Attachment.AttachmentType.FILE
            );

            // 새 파일 저장
            for (PostUpdateRequest.FileRequest fileReq : request.getFiles()) {
                Attachment attachment = Attachment.builder()
                        .targetType(Attachment.TargetType.POST)
                        .targetId(postId)
                        .attachmentType(Attachment.AttachmentType.FILE)
                        .fileName(fileReq.getFileName())
                        .fileSize(fileReq.getFileSize())
                        .filePath(fileReq.getFilePath())
                        .contentType(fileReq.getContentType())
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

    /**
     * 게시글 삭제 (Soft Delete)
     */
    @Transactional
    public void deletePost(Long projectId, Long postId) {
        // Post 조회 및 검증
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 작성자 권한 검증
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        if (!post.getUser().getId().equals(userDetails.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // projectId 검증
        if (!post.getStep().getProject().getId().equals(projectId)) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        // 이미 삭제된 게시글인지 확인
        if (post.getStatus() == PostApprovalStatus.DELETED) {
            throw new BusinessException(ErrorCode.POST_ALREADY_DELETED);
        }

        // Soft Delete: status를 DELETED로 변경하고 deletedAt 설정
        post.updateStatus(PostApprovalStatus.DELETED);
        post.softDelete();
    }

}
