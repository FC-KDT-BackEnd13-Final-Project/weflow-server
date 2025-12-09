package com.rdc.weflow_server.service.user;

import com.rdc.weflow_server.dto.user.request.*;
import com.rdc.weflow_server.dto.user.response.UserResponse;
import com.rdc.weflow_server.entity.company.Company;
import com.rdc.weflow_server.entity.log.ActionType;
import com.rdc.weflow_server.entity.log.TargetTable;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.company.CompanyRepository;
import com.rdc.weflow_server.repository.user.UserRepository;
import com.rdc.weflow_server.service.log.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;

    /**
     * 관리자 - 회원 생성
     * POST /api/admin/users
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request, Long adminId, String ipAddress) {
        // 1. 회사 존재 확인
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        // 2. 이메일 중복 체크
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
        user.updateMyInfo(request.getName(), request.getPhoneNumber());

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

        // 5. 로그 기록
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

        // 4. 정보 수정 (관리자용 메서드 호출)
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
}