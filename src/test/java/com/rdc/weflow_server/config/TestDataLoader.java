package com.rdc.weflow_server.config;

import com.rdc.weflow_server.entity.comment.Comment;
import com.rdc.weflow_server.entity.company.Company;
import com.rdc.weflow_server.entity.company.CompanyStatus;
import com.rdc.weflow_server.entity.post.Post;
import com.rdc.weflow_server.entity.post.PostApprovalStatus;
import com.rdc.weflow_server.entity.post.PostOpenStatus;
import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import com.rdc.weflow_server.entity.step.Step;
import com.rdc.weflow_server.entity.step.StepStatus;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.entity.user.UserRole;
import com.rdc.weflow_server.entity.user.UserStatus;
import com.rdc.weflow_server.repository.comment.CommentRepository;
import com.rdc.weflow_server.repository.company.CompanyRepository;
import com.rdc.weflow_server.repository.post.PostRepository;
import com.rdc.weflow_server.repository.project.ProjectRepository;
import com.rdc.weflow_server.repository.step.StepRepository;
import com.rdc.weflow_server.repository.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Profile("test")
public class TestDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ProjectRepository projectRepository;
    private final StepRepository stepRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    public TestDataLoader(UserRepository userRepository,
                          CompanyRepository companyRepository,
                          ProjectRepository projectRepository,
                          StepRepository stepRepository,
                          PostRepository postRepository,
                          CommentRepository commentRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.projectRepository = projectRepository;
        this.stepRepository = stepRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 이미 post가 있으면 아무것도 안 함
        if (postRepository.count() > 0) {
            System.out.println("========================================");
            System.out.println("테스트 데이터가 이미 존재합니다.");
            System.out.println("========================================");
            return;
        }

        // Company 생성 또는 조회
        Company company = companyRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    Company newCompany = Company.builder()
                            .name("BN-SYSTEM")
                            .businessNumber("787-87-01752")
                            .representative("엄요한")
                            .email("best@bn-system.com")
                            .address("서울특별시 노원구 동일로174길 27 (공릉동, 서울창업디딤터) 303호")
                            .status(CompanyStatus.ACTIVE)
                            .build();
                    return companyRepository.save(newCompany);
                });

        // admin 계정 생성 또는 조회
        User admin = userRepository.findByEmail("admin@bn-system.com")
                .orElseGet(() -> {
                    User newAdmin = User.builder()
                            .email("admin@bn-system.com")
                            .password(passwordEncoder.encode("admin123"))
                            .name("시스템관리자")
                            .phoneNumber("010-0000-0000")
                            .role(UserRole.SYSTEM_ADMIN)
                            .status(UserStatus.ACTIVE)
                            .isTemporaryPassword(true)
                            .company(company)
                            .build();
                    return userRepository.save(newAdmin);
                });

        // 1. 테스트 프로젝트 만들기
        Project testProject = Project.builder()
                .name("샘플 프로젝트 - 홈페이지 리뉴얼")
                .description("테스트를 위한 샘플 프로젝트입니다")
                .status(ProjectStatus.IN_PROGRESS)
                .startDate(LocalDateTime.now().minusDays(30))
                .expectedEndDate(LocalDateTime.now().plusDays(60))
                .contractPrice(new BigDecimal("10000000"))
                .createdBy(admin.getId())
                .company(admin.getCompany())
                .build();
        projectRepository.save(testProject);

        // 2. Step 만들기 (여러 단계)
        Step step1 = Step.builder()
                .phase(ProjectStatus.CONTRACT)
                .title("계약 및 요구사항 정의")
                .description("프로젝트 계약 및 초기 요구사항을 정의하는 단계")
                .orderIndex(1)
                .status(StepStatus.APPROVED)
                .project(testProject)
                .createdBy(admin)
                .build();
        stepRepository.save(step1);

        Step step2 = Step.builder()
                .phase(ProjectStatus.IN_PROGRESS)
                .title("기획 및 설계")
                .description("화면 기획 및 시스템 설계")
                .orderIndex(2)
                .status(StepStatus.IN_PROGRESS)
                .project(testProject)
                .createdBy(admin)
                .build();
        stepRepository.save(step2);

        Step step3 = Step.builder()
                .phase(ProjectStatus.IN_PROGRESS)
                .title("디자인")
                .description("UI/UX 디자인 작업")
                .orderIndex(3)
                .status(StepStatus.PENDING)
                .project(testProject)
                .createdBy(admin)
                .build();
        stepRepository.save(step3);

        Step step4 = Step.builder()
                .phase(ProjectStatus.IN_PROGRESS)
                .title("개발")
                .description("프론트엔드 및 백엔드 개발")
                .orderIndex(4)
                .status(StepStatus.PENDING)
                .project(testProject)
                .createdBy(admin)
                .build();
        stepRepository.save(step4);

        Step step5 = Step.builder()
                .phase(ProjectStatus.DELIVERY)
                .title("테스트 및 배포")
                .description("QA 테스트 및 운영 배포")
                .orderIndex(5)
                .status(StepStatus.PENDING)
                .project(testProject)
                .createdBy(admin)
                .build();
        stepRepository.save(step5);

        // 3. 게시글 만들기
        Post post1 = Post.builder()
                .title("홈페이지 메인 페이지 레이아웃 확인 부탁드립니다")
                .content("메인 페이지 레이아웃을 다음과 같이 구성했습니다.\n\n1. 상단 네비게이션\n2. 메인 비주얼 배너\n3. 주요 서비스 소개\n4. 최근 소식\n5. 푸터\n\n검토 후 의견 부탁드립니다.")
                .projectStatus(ProjectStatus.IN_PROGRESS)
                .status(PostApprovalStatus.WAITING_CONFIRM)
                .openStatus(PostOpenStatus.OPEN)
                .step(step2)
                .user(admin)
                .build();
        postRepository.save(post1);

        Post post2 = Post.builder()
                .title("요구사항 정의서 최종 확정")
                .content("고객사와 협의한 요구사항 정의서를 첨부합니다.\n\n주요 기능:\n- 회원 관리\n- 게시판\n- 예약 시스템\n- 결제 연동\n\n해당 내용으로 진행하도록 하겠습니다.")
                .projectStatus(ProjectStatus.CONTRACT)
                .status(PostApprovalStatus.CONFIRMED)
                .openStatus(PostOpenStatus.CLOSED)
                .step(step1)
                .user(admin)
                .build();
        postRepository.save(post2);

        Post post3 = Post.builder()
                .title("디자인 시안 공유")
                .content("첫 번째 디자인 시안을 공유드립니다.\n\n메인 컬러는 블루 계열로 선정했으며,\n전체적으로 모던하고 깔끔한 느낌을 주도록 작업했습니다.")
                .projectStatus(ProjectStatus.IN_PROGRESS)
                .status(PostApprovalStatus.NORMAL)
                .openStatus(PostOpenStatus.OPEN)
                .step(step3)
                .user(admin)
                .build();
        postRepository.save(post3);

        // 4. 댓글 만들기
        Comment comment1 = Comment.builder()
                .content("레이아웃 잘 확인했습니다. 메인 비주얼 배너의 높이를 조금 낮춰주시면 좋을 것 같습니다.")
                .post(post1)
                .user(admin)
                .build();
        commentRepository.save(comment1);

        Comment comment2 = Comment.builder()
                .content("알겠습니다. 내일 수정본 올려드리겠습니다.")
                .post(post1)
                .user(admin)
                .parentComment(comment1)
                .build();
        commentRepository.save(comment2);

        Comment comment3 = Comment.builder()
                .content("요구사항 정의서 확인했습니다. 일정대로 진행 부탁드립니다!")
                .post(post2)
                .user(admin)
                .build();
        commentRepository.save(comment3);

        Comment comment4 = Comment.builder()
                .content("디자인 시안 좋습니다! 다만 폰트를 좀 더 가독성 좋은 것으로 변경해주세요.")
                .post(post3)
                .user(admin)
                .build();
        commentRepository.save(comment4);

        System.out.println("========================================");
        System.out.println("테스트 데이터 생성 완료!");
        System.out.println("테스트 프로젝트: " + testProject.getName());
        System.out.println("생성된 Step 수: 5개");
        System.out.println("생성된 게시글 수: 3개");
        System.out.println("생성된 댓글 수: 4개");
        System.out.println("========================================");
    }
}
