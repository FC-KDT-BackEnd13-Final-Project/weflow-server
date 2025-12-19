package com.rdc.weflow_server.service.post;

import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.post.*;
import com.rdc.weflow_server.entity.attachment.Attachment;
import com.rdc.weflow_server.entity.comment.Comment;
import com.rdc.weflow_server.entity.notification.NotificationType;
import com.rdc.weflow_server.entity.post.*;
import com.rdc.weflow_server.entity.project.ProjectMember;
import com.rdc.weflow_server.entity.project.ProjectPhase;
import com.rdc.weflow_server.entity.step.Step;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.entity.user.UserRole;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.attachment.AttachmentRepository;
import com.rdc.weflow_server.repository.comment.CommentRepository;
import com.rdc.weflow_server.repository.post.PostAnswerRepository;
import com.rdc.weflow_server.repository.post.PostQuestionRepository;
import com.rdc.weflow_server.repository.post.PostRepository;
import com.rdc.weflow_server.repository.project.ProjectMemberRepository;
import com.rdc.weflow_server.repository.step.StepRepository;
import com.rdc.weflow_server.service.log.ActivityLogService;
import com.rdc.weflow_server.entity.log.ActionType;
import com.rdc.weflow_server.entity.log.TargetTable;
import com.rdc.weflow_server.service.notification.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final AttachmentRepository attachmentRepository;
    private final PostQuestionRepository postQuestionRepository;
    private final PostAnswerRepository postAnswerRepository;
    private final StepRepository stepRepository;
    private final CommentRepository commentRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;
    private final ObjectMapper objectMapper;

    /**
     * 게시글 상세 조회
     * TODO: 나중에 fetch join 등으로 최적화 필요.
     */
    public PostDetailResponse getPost(Long projectId, Long postId) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
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
                            .map(answer -> {
                                List<Long> selectedOptionIds = null;
                                String textInput = null;

                                // JSON 파싱
                                try {
                                    Map<String, Object> answerData = objectMapper.readValue(
                                            answer.getContent(),
                                            new TypeReference<Map<String, Object>>() {}
                                    );

                                    if (answerData.containsKey("selectedOptionIds")) {
                                        selectedOptionIds = ((List<?>) answerData.get("selectedOptionIds"))
                                                .stream()
                                                .map(id -> ((Number) id).longValue())
                                                .collect(Collectors.toList());
                                    }
                                    if (answerData.containsKey("textInput")) {
                                        textInput = (String) answerData.get("textInput");
                                    }
                                } catch (Exception e) {
                                    // JSON 파싱 실패 시 null 유지
                                }

                                return PostDetailResponse.AnswerDto.builder()
                                        .selectedOptionIds(selectedOptionIds)
                                        .textInput(textInput)
                                        .respondent(PostDetailResponse.RespondentDto.builder()
                                                .memberId(answer.getUser().getId())
                                                .name(answer.getUser().getName())
                                                .build())
                                        .respondedAt(answer.getCreatedAt())
                                        .build();
                            })
                            .orElse(null);

                    // 옵션 조회
                    List<PostDetailResponse.QuestionOptionDto> optionDtos = q.getOptions().stream()
                            .map(option -> PostDetailResponse.QuestionOptionDto.builder()
                                    .optionId(option.getId())
                                    .optionText(option.getOptionText())
                                    .hasInput(option.getHasInput())
                                    .build())
                            .toList();

                    return PostDetailResponse.QuestionDto.builder()
                            .questionId(q.getId())
                            .content(q.getQuestionText())
                            .questionType(q.getQuestionType().name())
                            .options(optionDtos)
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
                .openStatus(post.getOpenStatus())
                .author(PostDetailResponse.AuthorDto.builder()
                        .memberId(post.getUser().getId())
                        .name(post.getUser().getName())
                        .role(post.getUser().getRole().name())
                        .companyName(companyName)
                        .build())
                .projectPhase(post.getProjectPhase())
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
    public PostListResponse getPosts(Long projectId, ProjectPhase projectPhase, PostOpenStatus openStatus, Long stepId, Pageable pageable) {
        Page<Post> postPage;

        // 필터에 따라 게시글 조회
        if (stepId != null && openStatus != null) {
            postPage = postRepository.findByStepIdAndOpenStatusAndDeletedAtIsNull(stepId, openStatus, pageable);
        } else if (stepId != null) {
            postPage = postRepository.findByStepIdAndDeletedAtIsNull(stepId, pageable);
        } else if (projectPhase != null && openStatus != null) {
            postPage = postRepository.findByStepProjectIdAndProjectPhaseAndOpenStatusAndDeletedAtIsNull(projectId, projectPhase, openStatus, pageable);
        } else if (projectPhase != null) {
            postPage = postRepository.findByStepProjectIdAndProjectPhaseAndDeletedAtIsNull(projectId, projectPhase, pageable);
        } else if (openStatus != null) {
            postPage = postRepository.findByStepProjectIdAndOpenStatusAndDeletedAtIsNull(projectId, openStatus, pageable);
        } else {
            postPage = postRepository.findByStepProjectIdAndDeletedAtIsNull(projectId, pageable);
        }

        // PostListResponse로 변환
        List<PostListResponse.PostItem> postItems = postPage.getContent().stream()
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
                    boolean hasQuestions = !postQuestionRepository.findByPostId(post.getId()).isEmpty();

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
                            .projectPhase(post.getProjectPhase())
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

        // 페이지네이션 정보 생성
        PostListResponse.PageInfo pageInfo = PostListResponse.PageInfo.builder()
                .currentPage(postPage.getNumber())
                .pageSize(postPage.getSize())
                .totalElements(postPage.getTotalElements())
                .totalPages(postPage.getTotalPages())
                .hasNext(postPage.hasNext())
                .hasPrevious(postPage.hasPrevious())
                .build();

        return PostListResponse.builder()
                .posts(postItems)
                .pageInfo(pageInfo)
                .build();
    }

    /**
     * 게시글 작성
     */
    @Transactional
    public PostCreateResponse createPost(Long projectId, PostCreateRequest request, HttpServletRequest httpRequest) {
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
            parentPost = postRepository.findByIdAndDeletedAtIsNull(request.getParentPostId())
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
                .projectPhase(request.getProjectPhase())
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
                // QuestionType enum으로 변환 (유효성 검사)
                QuestionType questionType;
                try {
                    questionType = QuestionType.valueOf(questionReq.getQuestionType().toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new BusinessException(ErrorCode.INVALID_QUESTION_TYPE);
                }

                PostQuestion question = PostQuestion.builder()
                        .post(post)
                        .questionText(questionReq.getQuestionText())
                        .questionType(questionType)
                        .build();

                // 옵션 추가 (cascade로 자동 저장)
                if (questionReq.getOptions() != null && !questionReq.getOptions().isEmpty()) {
                    for (PostCreateRequest.QuestionOptionRequest optionReq : questionReq.getOptions()) {
                        PostQuestionOption option = PostQuestionOption.builder()
                                .question(question)
                                .optionText(optionReq.getOptionText())
                                .hasInput(optionReq.getHasInput() != null ? optionReq.getHasInput() : false)
                                .build();
                        question.getOptions().add(option);
                    }
                }
                postQuestionRepository.save(question);
            }
        }

        // 로그 기록
        activityLogService.createLog(
                ActionType.CREATE,
                TargetTable.POST,
                post.getId(),
                user.getId(),
                projectId,
                httpRequest.getRemoteAddr()
        );

        // 알림 발송 (작성자 본인 제외 프로젝트 멤버 전체에게 알림)
        List<User> projectMembers = projectMemberRepository
                .findByProjectIdAndDeletedAtIsNull(projectId).stream()
                .map(ProjectMember::getUser)
                .filter(member -> !member.getId().equals(user.getId())) // 본인 제외
                .toList();

        for (User member : projectMembers) {
            notificationService.send(
                    member,                         // Receiver (Entity 직접 전달)
                    NotificationType.NEW_POST,      // 답글도 똑같은 '게시글'로 취급
                    "새 게시글 작성",                // Title
                    user.getName() + "님이 \"" + post.getTitle() + "\" 게시글을 작성했습니다.", // Message
                    step.getProject(),              // Project
                    post,                           // Post
                    null                            // StepRequest
            );
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
    public PostDetailResponse updatePost(Long projectId, Long postId, PostUpdateRequest request, HttpServletRequest httpRequest) {
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

        // 반대편 role 참여 여부 확인
        User postAuthor = post.getUser();
        UserRole authorRole = postAuthor.getRole();
        UserRole oppositeRole = (authorRole == UserRole.AGENCY) ? UserRole.CLIENT : UserRole.AGENCY;

        // 1. 댓글에서 반대편 role 참여 확인
        boolean hasOppositeComment = commentRepository.findAllByPostId(postId).stream()
                .anyMatch(comment -> comment.getUser().getRole() == oppositeRole);

        // 2. 질문 답변에서 반대편 role 참여 확인
        List<PostQuestion> questions = postQuestionRepository.findByPostId(postId);
        boolean hasOppositeAnswer = questions.stream()
                .anyMatch(question -> postAnswerRepository.findByQuestionId(question.getId())
                        .map(answer -> answer.getUser().getRole() == oppositeRole)
                        .orElse(false));

        // 3. 답글(자식 게시글)에서 반대편 role 참여 확인
        boolean hasOppositeReply = post.getChildren().stream()
                .anyMatch(child -> child.getUser().getRole() == oppositeRole);

        // 반대편이 참여했다면 수정 불가
        if (hasOppositeComment || hasOppositeAnswer || hasOppositeReply) {
            throw new BusinessException(ErrorCode.POST_CANNOT_EDIT);
        }

        // 제목 및 내용 수정
        if (request.getTitle() != null) {
            post.updateTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            post.updateContent(request.getContent());
        }

        // 파일 처리: fileId가 있으면 기존 파일 유지, 없으면 새 파일
        if (request.getFiles() != null) {
            // 요청에 포함된 파일 ID 목록
            List<Long> requestedFileIds = request.getFiles().stream()
                    .map(PostUpdateRequest.FileRequest::getFileId)
                    .filter(id -> id != null)
                    .toList();

            // 기존 파일 중 요청에 없는 파일은 삭제
            List<Attachment> existingFiles = attachmentRepository
                    .findByTargetTypeAndTargetId(Attachment.TargetType.POST, postId).stream()
                    .filter(a -> a.getAttachmentType() == Attachment.AttachmentType.FILE)
                    .toList();

            for (Attachment existingFile : existingFiles) {
                if (!requestedFileIds.contains(existingFile.getId())) {
                    attachmentRepository.delete(existingFile);
                }
            }

            // 새 파일 저장 (fileId가 null인 경우만)
            for (PostUpdateRequest.FileRequest fileReq : request.getFiles()) {
                if (fileReq.getFileId() == null) {
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
        }

        // 링크 처리: linkId가 있으면 기존 링크 유지, 없으면 새 링크
        if (request.getLinks() != null) {
            // 요청에 포함된 링크 ID 목록
            List<Long> requestedLinkIds = request.getLinks().stream()
                    .map(PostUpdateRequest.LinkRequest::getLinkId)
                    .filter(id -> id != null)
                    .toList();

            // 기존 링크 중 요청에 없는 링크는 삭제
            List<Attachment> existingLinks = attachmentRepository
                    .findByTargetTypeAndTargetId(Attachment.TargetType.POST, postId).stream()
                    .filter(a -> a.getAttachmentType() == Attachment.AttachmentType.LINK)
                    .toList();

            for (Attachment existingLink : existingLinks) {
                if (!requestedLinkIds.contains(existingLink.getId())) {
                    attachmentRepository.delete(existingLink);
                }
            }

            // 새 링크 저장 (linkId가 null인 경우만)
            for (PostUpdateRequest.LinkRequest linkReq : request.getLinks()) {
                if (linkReq.getLinkId() == null) {
                    Attachment link = Attachment.builder()
                            .targetType(Attachment.TargetType.POST)
                            .targetId(postId)
                            .attachmentType(Attachment.AttachmentType.LINK)
                            .url(linkReq.getUrl())
                            .build();
                    attachmentRepository.save(link);
                }
            }
        }

        // 기존 질문 삭제
        if (request.getQuestions() != null) {
            postQuestionRepository.deleteByPostId(postId);

            // 새 질문 저장
            for (PostUpdateRequest.QuestionRequest questionReq : request.getQuestions()) {
                // QuestionType enum으로 변환 (유효성 검사)
                QuestionType questionType;
                try {
                    questionType = QuestionType.valueOf(questionReq.getQuestionType().toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new BusinessException(ErrorCode.INVALID_QUESTION_TYPE);
                }

                PostQuestion question = PostQuestion.builder()
                        .post(post)
                        .questionText(questionReq.getQuestionText())
                        .questionType(questionType)
                        .build();

                // 옵션 추가 (cascade로 자동 저장)
                if (questionReq.getOptions() != null && !questionReq.getOptions().isEmpty()) {
                    for (PostUpdateRequest.QuestionOptionRequest optionReq : questionReq.getOptions()) {
                        PostQuestionOption option = PostQuestionOption.builder()
                                .question(question)
                                .optionText(optionReq.getOptionText())
                                .hasInput(optionReq.getHasInput() != null ? optionReq.getHasInput() : false)
                                .build();
                        question.getOptions().add(option);
                    }
                }
                postQuestionRepository.save(question);
            }

            // 질문 유무에 따라 상태 업데이트
            PostApprovalStatus newStatus = request.getQuestions().isEmpty()
                    ? PostApprovalStatus.NORMAL
                    : PostApprovalStatus.WAITING_CONFIRM;
            post.updateStatus(newStatus);
        }

        // 로그 기록
        activityLogService.createLog(
                ActionType.UPDATE,
                TargetTable.POST,
                postId,
                userDetails.getId(),
                projectId,
                httpRequest.getRemoteAddr()
        );

        // 수정된 게시글 상세 정보 반환
        return getPost(projectId, postId);
    }

    /**
     * 게시글 삭제 (Soft Delete)
     */
    @Transactional
    public void deletePost(Long projectId, Long postId, HttpServletRequest httpRequest) {
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

        // 게시글의 모든 댓글도 함께 Soft Delete
        List<Comment> comments = commentRepository.findAllByPostId(postId);
        comments.forEach(Comment::softDelete);

        // Soft Delete: status를 DELETED로 변경하고 deletedAt 설정
        post.updateStatus(PostApprovalStatus.DELETED);
        post.softDelete();

        // 로그 기록
        activityLogService.createLog(
                ActionType.DELETE,
                TargetTable.POST,
                postId,
                userDetails.getId(),
                projectId,
                httpRequest.getRemoteAddr()
        );
    }

    /**
     * 질문에 답변하기
     */
    @Transactional
    public PostAnswerResponse answerQuestion(Long projectId, Long postId, Long questionId, PostAnswerRequest request, HttpServletRequest httpRequest) {
        // 질문 조회
        PostQuestion question = postQuestionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));

        // 질문이 해당 게시글에 속하는지 검증
        if (!question.getPost().getId().equals(postId)) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND);
        }

        // 게시글 조회
        Post post = question.getPost();

        // projectId 검증
        if (!post.getStep().getProject().getId().equals(projectId)) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        // 현재 사용자 조회
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        User currentUser = userDetails.getUser();

        // 권한 검증: 작성자의 반대편 역할만 답변 가능
        User postAuthor = post.getUser();
        UserRole authorRole = postAuthor.getRole();
        UserRole currentRole = currentUser.getRole();

        // SYSTEM_ADMIN은 모든 질문에 답변 가능
        if (currentRole != UserRole.SYSTEM_ADMIN) {
            // CLIENT가 쓴 글 -> AGENCY만 답변 가능
            // AGENCY가 쓴 글 -> CLIENT만 답변 가능
            if (authorRole == UserRole.CLIENT && currentRole != UserRole.AGENCY) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
            if (authorRole == UserRole.AGENCY && currentRole != UserRole.CLIENT) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
        }

        // 이미 답변이 있는지 확인
        if (postAnswerRepository.findByQuestionId(questionId).isPresent()) {
            throw new BusinessException(ErrorCode.ANSWER_ALREADY_EXISTS);
        }

        // 답변 데이터를 JSON으로 변환
        Map<String, Object> answerData = new HashMap<>();
        if (request.getSelectedOptionIds() != null && !request.getSelectedOptionIds().isEmpty()) {
            answerData.put("selectedOptionIds", request.getSelectedOptionIds());
        }
        if (request.getTextInput() != null && !request.getTextInput().trim().isEmpty()) {
            answerData.put("textInput", request.getTextInput());
        }

        String answerJson;
        try {
            answerJson = objectMapper.writeValueAsString(answerData);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 답변 생성 (answerType은 deprecated되었지만 호환성을 위해 YES로 설정)
        PostAnswer answer = PostAnswer.builder()
                .answerType(AnswerType.YES)
                .content(answerJson)
                .question(question)
                .user(currentUser)
                .build();
        postAnswerRepository.save(answer);

        // 로그 기록
        activityLogService.createLog(
                ActionType.CREATE,
                TargetTable.POST_ANSWER,
                answer.getId(),
                currentUser.getId(),
                projectId,
                httpRequest.getRemoteAddr()
        );

        // 알림 발송 (게시글 작성자에게만 알림)
        if (!postAuthor.getId().equals(currentUser.getId())) {
            notificationService.send(
                    postAuthor,                     // Receiver (질문한 사람 = 게시글 작성자)
                    NotificationType.NEW_COMMENT,   // Type (답변도 댓글/반응으로 취급)
                    "새 답변 등록",                   // Title
                    currentUser.getName() + "님이 질문에 답변을 남겼습니다.", // Message
                    post.getStep().getProject(),    // Project Entity
                    post,                           // Post Entity
                    null                            // StepRequest
            );
        }

        // Response 생성 (content는 JSON 형식)
        return PostAnswerResponse.builder()
                .answerId(answer.getId())
                .answerType(answer.getAnswerType())
                .content(answerJson)
                .respondent(PostAnswerResponse.RespondentDto.builder()
                        .memberId(currentUser.getId())
                        .name(currentUser.getName())
                        .role(currentUser.getRole().name())
                        .build())
                .createdAt(answer.getCreatedAt())
                .build();
    }

    /**
     * 게시글 상태 변경 (CONFIRMED / REJECTED)
     */
    @Transactional
    public void updatePostStatus(Long projectId, Long postId, PostStatusUpdateRequest request) {
        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // projectId 검증
        if (!post.getStep().getProject().getId().equals(projectId)) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        // 작성자 권한 검증
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        if (!post.getUser().getId().equals(userDetails.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // WAITING_CONFIRM 상태일 때만 변경 가능
        if (post.getStatus() != PostApprovalStatus.WAITING_CONFIRM) {
            throw new BusinessException(ErrorCode.INVALID_POST_STATUS);
        }

        // CONFIRMED 또는 REJECTED만 허용
        PostApprovalStatus newStatus = request.getStatus();
        if (newStatus != PostApprovalStatus.CONFIRMED && newStatus != PostApprovalStatus.REJECTED) {
            throw new BusinessException(ErrorCode.INVALID_POST_STATUS);
        }

        // 상태 변경
        post.updateStatus(newStatus);
    }

    /**
     * 게시글 완료 (OPEN -> CLOSED)
     */
    @Transactional
    public void closePost(Long projectId, Long postId) {
        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // projectId 검증
        if (!post.getStep().getProject().getId().equals(projectId)) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        // 작성자 권한 검증
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        if (!post.getUser().getId().equals(userDetails.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 이미 CLOSED 상태인지 확인
        if (post.getOpenStatus() == PostOpenStatus.CLOSED) {
            throw new BusinessException(ErrorCode.POST_ALREADY_CLOSED);
        }

        // CLOSED 상태로 변경
        post.closePost();
    }

}
