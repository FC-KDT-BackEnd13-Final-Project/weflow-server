package com.rdc.weflow_server.service.user;

import com.rdc.weflow_server.dto.user.request.*;
import com.rdc.weflow_server.dto.user.response.CsvUserBatchResponse;
import com.rdc.weflow_server.dto.user.response.CsvUserFailure;
import com.rdc.weflow_server.dto.user.response.UserResponse;
import com.rdc.weflow_server.entity.company.Company;
import com.rdc.weflow_server.entity.company.CompanyType;
import com.rdc.weflow_server.entity.log.ActionType;
import com.rdc.weflow_server.entity.log.TargetTable;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.entity.user.UserRole;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.company.CompanyRepository;
import com.rdc.weflow_server.repository.user.UserRepository;
import com.rdc.weflow_server.service.log.ActivityLogService;
import com.rdc.weflow_server.service.notification.NotificationService;
import com.rdc.weflow_server.entity.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;
    private final PlatformTransactionManager transactionManager;
    private final NotificationService notificationService;

    /**
     * 이메일 중복 검사 (리스트)
     * POST /api/admin/users/check-emails
     */
    public List<String> checkDuplicateEmails(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return new ArrayList<>();
        }
        return userRepository.findExistingEmails(emails);
    }

    /**
     * 사용자 역할과 회사 유형 일치 검증
     */
    private void validateRoleCompanyTypeMatch(UserRole role, Company company) {
        // SYSTEM_ADMIN은 회사가 없어도 됨
        if (role == UserRole.SYSTEM_ADMIN) {
            return;
        }

        // AGENCY, CLIENT는 회사 필수
        if (company == null) {
            throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
        }

        // 회사 유형이 설정되지 않은 경우 에러
        if (company.getCompanyType() == null) {
            throw new BusinessException(ErrorCode.COMPANY_TYPE_NOT_SET);
        }

        // 역할과 회사 유형 매칭 검증
        if (role == UserRole.AGENCY && company.getCompanyType() != CompanyType.AGENCY) {
            throw new BusinessException(ErrorCode.USER_ROLE_COMPANY_TYPE_MISMATCH);
        }

        if (role == UserRole.CLIENT && company.getCompanyType() != CompanyType.CLIENT) {
            throw new BusinessException(ErrorCode.USER_ROLE_COMPANY_TYPE_MISMATCH);
        }
    }

    /**
     * 관리자 - 회원 생성
     * POST /api/admin/users
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request, Long adminId, String ipAddress) {
        // 1. 회사 존재 확인
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        // 2. 역할과 회사 유형 일치 검증
        validateRoleCompanyTypeMatch(request.getRole(), company);

        // 3. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.USER_EMAIL_DUPLICATE);
        }

        // 3. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 4. DTO → Entity 변환
        User user = request.toEntity(company, encodedPassword);

        // 5. 저장
        User savedUser = userRepository.save(user);

        // 6. 로그 기록
        activityLogService.createLog(
                ActionType.CREATE,
                TargetTable.USER,
                savedUser.getId(),
                adminId,
                null,
                ipAddress
        );

        // 7. Entity → Response 변환
        return UserResponse.from(savedUser);
    }

    /**
     * 관리자 - 회원 일괄 생성
     * POST /api/admin/users/batch
     */
    @Transactional
    public List<UserResponse> createUsersBatch(List<CreateUserRequest> requests, Long adminId, String ipAddress) {
        // 기존 단건 생성 메서드(createUser)를 재활용하여 반복 처리
        return requests.stream()
                .map(request -> createUser(request, adminId, ipAddress))
                .collect(Collectors.toList());
    }

    /**
     * CSV 파일을 통한 회원 일괄 생성 (Partial Success)
     * POST /api/admin/users/batch/csv
     */
    @Transactional
    public CsvUserBatchResponse createUsersBatchFromCsv(
            MultipartFile file,
            Long companyId,
            String password,
            Long adminId,
            String ipAddress) {

        // 1. 비밀번호 검증
        if (password == null || password.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호는 필수입니다.");
        }

        // 2. CSV 파일 검증
        validateCsvFile(file);

        // 3. 회사 조회 및 CompanyType 확인
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        if (company.getCompanyType() == null) {
            throw new BusinessException(ErrorCode.COMPANY_TYPE_NOT_SET);
        }

        // 4. CompanyType에 따른 UserRole 결정
        UserRole roleToAssign = determineUserRole(company.getCompanyType());

        // 5. TransactionTemplate 생성 (개별 트랜잭션 관리)
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // 6. CSV 파싱 및 처리
        List<UserResponse> successUsers = new ArrayList<>();
        List<CsvUserFailure> failures = new ArrayList<>();
        int rowNumber = 1; // 헤더 제외, 1부터 시작

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .builder()
                     .setHeader() // 파일의 첫 줄을 헤더로 자동 인식
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build())) {

            for (CSVRecord record : csvParser) {
                try {
                    // 각 회원마다 새로운 트랜잭션으로 처리
                    UserResponse userResponse = transactionTemplate.execute(status ->
                            processUserRecord(record, roleToAssign, company, password, adminId, ipAddress)
                    );
                    successUsers.add(userResponse);

                } catch (BusinessException e) {
                    // 비즈니스 예외는 실패 목록에 추가
                    failures.add(CsvUserFailure.builder()
                            .rowNumber(rowNumber)
                            .email(record.isMapped("이메일") ? record.get("이메일") : "N/A")
                            .name(record.isMapped("이름") ? record.get("이름") : "N/A")
                            .reason(e.getMessage())
                            .build());

                } catch (Exception e) {
                    // 기타 예외 (파싱 오류 등)
                    failures.add(CsvUserFailure.builder()
                            .rowNumber(rowNumber)
                            .email(record.isMapped("이메일") ? record.get("이메일") : "N/A")
                            .name(record.isMapped("이름") ? record.get("이름") : "N/A")
                            .reason("CSV 형식 오류: " + e.getMessage())
                            .build());
                }
                rowNumber++;
            }

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "CSV 파일 읽기 실패");
        }

        // 7. 응답 생성
        int totalCount = successUsers.size() + failures.size();
        return CsvUserBatchResponse.builder()
                .totalCount(totalCount)
                .successCount(successUsers.size())
                .failureCount(failures.size())
                .successUsers(successUsers)
                .failures(failures)
                .build();
    }

    /**
     * CSV 레코드를 User로 변환 및 저장
     * TransactionTemplate으로 관리되는 개별 트랜잭션
     */
    private UserResponse processUserRecord(
            CSVRecord record,
            UserRole role,
            Company company,
            String password,
            Long adminId,
            String ipAddress) {

        // 1. CSV 레코드에서 값 추출
        String name = record.get("이름");
        String email = record.get("이메일");
        String phoneNumber = record.get("전화번호");

        // 2. 필수 값 검증
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이름은 필수입니다.");
        }
        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이메일은 필수입니다.");
        }

        // 3. 이메일 형식 검증
        if (!isValidEmail(email)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "올바른 이메일 형식이 아닙니다.");
        }

        // 4. 이메일 중복 체크
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.USER_EMAIL_DUPLICATE);
        }

        // 5. CSV Injection 방지
        sanitizeInputs(name, email, phoneNumber);

        // 6. CreateUserRequest DTO 생성
        CreateUserRequest request = new CreateUserRequest(
                name.trim(),
                email.trim(),
                password.trim(),
                phoneNumber != null && !phoneNumber.isBlank() ? phoneNumber.trim() : null,
                role,
                company.getId()
        );

        // 7. 기존 createUser 로직 재사용
        return createUser(request, adminId, ipAddress);
    }

    /**
     * CSV 파일 유효성 검증
     */
    private void validateCsvFile(MultipartFile file) {
        // 파일 존재 확인
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "CSV 파일이 비어있습니다.");
        }

        // 파일 크기 제한 (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "CSV 파일 크기는 5MB를 초과할 수 없습니다.");
        }

        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".csv")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "CSV 파일만 업로드 가능합니다.");
        }

        // Content-Type 검증 (선택적, 브라우저마다 다를 수 있음)
        String contentType = file.getContentType();
        if (contentType != null &&
                !contentType.equals("text/csv") &&
                !contentType.equals("application/csv") &&
                !contentType.equals("application/vnd.ms-excel")) {
            log.warn("비표준 CSV Content-Type: {}", contentType);
        }
    }

    /**
     * CompanyType에 따른 UserRole 결정
     */
    private UserRole determineUserRole(CompanyType companyType) {
        return switch (companyType) {
            case AGENCY -> UserRole.AGENCY;
            case CLIENT -> UserRole.CLIENT;
        };
    }

    /**
     * 이메일 형식 검증 (간단한 정규식)
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * CSV Injection 방지 (=, +, -, @ 시작 값 차단)
     */
    private void sanitizeInputs(String... inputs) {
        for (String input : inputs) {
            if (input != null && !input.isEmpty()) {
                char firstChar = input.charAt(0);
                if (firstChar == '=' || firstChar == '+' || firstChar == '-' || firstChar == '@') {
                    throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                            "CSV Injection 공격이 감지되었습니다.");
                }
            }
        }
    }

    /**
     * 내 정보 조회
     * GET /api/users/me
     */
    public UserResponse getMyInfo(Long userId) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 삭제된 사용자 체크
        if (user.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_DELETED);
        }

        // 3. Entity → Response 변환
        return UserResponse.from(user);
    }

    /**
     * 내 정보 수정
     * PATCH /api/users/me
     */
    @Transactional
    public UserResponse updateMyInfo(Long userId, UpdateUserRequest request, String ipAddress) {
        // 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 전화번호 중복 체크
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new BusinessException(ErrorCode.USER_PHONE_DUPLICATE);
            }
        }

        // 3. 정보 수정
        user.updateMyInfo(
                request.getName(),
                request.getPhoneNumber(),
                request.getIsEmailNotificationEnabled()
        );

        // 4. 로그 기록 (본인 수정)
        activityLogService.createLog(
                ActionType.UPDATE,
                TargetTable.USER,
                userId,
                userId,
                null,
                ipAddress
        );

        // 5. Entity → Response 변환
        return UserResponse.from(user);
    }

    /**
     * 비밀번호 변경
     * PATCH /api/users/me/password
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request, String ipAddress) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 새 비밀번호 일치 체크 (항상 필수)
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        // 3. 현재 비밀번호 검증 (임시 비밀번호 여부에 따라 분기)
        if (user.getIsTemporaryPassword()) {
            // [첫 로그인] 임시 비밀번호 사용자
            // → 현재 비밀번호 입력란 없음, 검증 생략
        } else {
            // [일반 사용자] 현재 비밀번호 검증 필수
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_PASSWORD);
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new BusinessException(ErrorCode.INVALID_PASSWORD);
            }
        }

        // 4. 비밀번호 변경 (암호화 + 임시 비밀번호 해제)
        user.changePassword(passwordEncoder.encode(request.getNewPassword()));

        // 5. 비밀번호 변경 알림 발송
        String notificationTitle = "비밀번호가 변경되었습니다";
        String notificationMessage = String.format(
            "%s님의 비밀번호가 성공적으로 변경되었습니다.\n" +
            "변경 시간: %s\n" +
            "접속 IP: %s\n\n" +
            "본인이 변경하지 않은 경우, 즉시 관리자에게 문의해주세요.",
            user.getName(),
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            ipAddress
        );

        notificationService.send(
            user,
            NotificationType.PASSWORD_CHANGED,
            notificationTitle,
            notificationMessage,
            null,  // project
            null,  // post
            null   // stepRequest
        );

        // 6. 로그 기록
        activityLogService.createLog(
                ActionType.UPDATE,
                TargetTable.USER,
                userId,
                userId,
                null,
                ipAddress
        );
    }

    /**
     * 관리자 - 회원 목록 조회 (검색/페이징)
     * GET /api/admin/users
     */
    public Page<UserResponse> getUsers(UserSearchCondition condition, Pageable pageable) {
        // 1. Repository(QueryDSL) 호출
        Page<User> userPage = userRepository.searchUsers(condition, pageable);

        // 2. Entity Page -> DTO Page 변환
        return userPage.map(UserResponse::from);
    }

    /**
     * 관리자 - 회원 정보 수정
     * PATCH /api/admin/users/{userId}
     */
    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserAdminRequest request, Long adminId, String ipAddress) {
        // 1. 수정할 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 전화번호 중복 체크 (변경 시에만)
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new BusinessException(ErrorCode.USER_PHONE_DUPLICATE);
            }
        }

        // 3. 회사 변경 시 조회 (변경 시에만)
        Company company = null;
        if (request.getCompanyId() != null) {
            company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
        }

        // 4. 역할과 회사 유형 일치 검증
        UserRole roleToValidate = request.getRole() != null ? request.getRole() : user.getRole();
        Company companyToValidate = request.getCompanyId() != null ? company : user.getCompany();
        validateRoleCompanyTypeMatch(roleToValidate, companyToValidate);

        // 5. 정보 수정 (관리자용 메서드 호출)
        user.updateByAdmin(
                request.getName(),
                request.getPhoneNumber(),
                request.getRole(),
                request.getStatus(),
                company
        );

        // 5. 로그 기록
        activityLogService.createLog(
                ActionType.UPDATE,
                TargetTable.USER,
                userId,
                adminId,
                null,
                ipAddress
        );

        return UserResponse.from(user);
    }

    /**
     * 관리자 - 회원 삭제 (Soft Delete)
     * DELETE /api/admin/users/{userId}
     */
    @Transactional
    public void deleteUser(Long userId, Long adminId, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 이미 삭제된 경우 예외 처리
        if (user.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_DELETED);
        }

        // Soft Delete 수행
        user.delete();

        // 로그 기록
        activityLogService.createLog(
                ActionType.DELETE,
                TargetTable.USER,
                userId,
                adminId,
                null,
                ipAddress
        );
    }

    /**
     * 관리자 - 회원 복구
     * PATCH /api/admin/users/{userId}/restore
     */
    @Transactional
    public UserResponse restoreUser(Long userId, Long adminId, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 삭제되지 않은 회원은 복구 불가
        if (user.getDeletedAt() == null) {
            throw new BusinessException(ErrorCode.USER_NOT_DELETED);
        }

        // 복구 수행
        user.restore();

        // 로그 기록
        activityLogService.createLog(
                ActionType.UPDATE,
                TargetTable.USER,
                userId,
                adminId,
                null,
                ipAddress
        );

        return UserResponse.from(user);
    }

    /**
     * 관리자 - 회원 비밀번호 강제 재설정
     * PATCH /api/admin/users/{userId}/reset-password
     */
    @Transactional
    public UserResponse resetPasswordByAdmin(Long userId, String newPassword, Long adminId, String ipAddress) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 삭제된 사용자 체크
        if (user.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_DELETED);
        }

        // 3. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(newPassword);

        // 4. 비밀번호 강제 재설정 (임시 비밀번호로 설정)
        user.resetPasswordByAdmin(encodedPassword);

        // 5. 비밀번호 재설정 알림 발송
        String notificationTitle = "비밀번호가 관리자에 의해 재설정되었습니다";
        String notificationMessage = String.format(
            "%s님의 비밀번호가 관리자에 의해 재설정되었습니다.\n" +
            "재설정 시간: %s\n\n" +
            "새로운 임시 비밀번호로 로그인한 후, 반드시 비밀번호를 변경해주세요.\n" +
            "임시 비밀번호: %s",
            user.getName(),
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            newPassword  // 암호화 전 평문 비밀번호
        );

        notificationService.send(
            user,
            NotificationType.PASSWORD_RESET_BY_ADMIN,
            notificationTitle,
            notificationMessage,
            null,  // project
            null,  // post
            null   // stepRequest
        );

        // 6. 로그 기록 (관리자가 비밀번호 재설정)
        activityLogService.createLog(
                ActionType.UPDATE,
                TargetTable.USER,
                userId,
                adminId,
                null,
                ipAddress
        );

        // 7. Entity → Response 변환
        return UserResponse.from(user);
    }
}