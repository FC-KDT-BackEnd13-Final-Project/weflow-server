package com.rdc.weflow_server.service.user;

import com.rdc.weflow_server.dto.user.request.CreateAdminUserRequest;
import com.rdc.weflow_server.dto.user.request.UpdateAdminUserRequest;
import com.rdc.weflow_server.dto.user.response.AdminUserResponse;
import com.rdc.weflow_server.entity.company.Company;
import com.rdc.weflow_server.entity.log.ActionType;
import com.rdc.weflow_server.entity.log.TargetTable;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.entity.user.UserRole;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.company.CompanyRepository;
import com.rdc.weflow_server.repository.user.UserRepository;
import com.rdc.weflow_server.service.log.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemAdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;
    private final CompanyRepository companyRepository;

    /** SYSTEM_ADMIN 생성 */
    @Transactional
    public AdminUserResponse createAdmin(CreateAdminUserRequest request, Long adminId, String ip) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.USER_EMAIL_DUPLICATE);
        }

        Company company = companyRepository.findById(1L)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        User admin = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.SYSTEM_ADMIN)
                .company(company)
                .build();

        User saved = userRepository.save(admin);

        activityLogService.createLog(
                ActionType.CREATE, TargetTable.USER,
                saved.getId(), adminId, null, ip
        );

        return AdminUserResponse.from(saved);
    }

    /** SYSTEM_ADMIN 목록 조회 */
    public List<AdminUserResponse> getAdmins() {
        return userRepository.findAllByRole(UserRole.SYSTEM_ADMIN).stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    /** SYSTEM_ADMIN 상세 조회 */
    public AdminUserResponse getAdminById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != UserRole.SYSTEM_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return AdminUserResponse.from(user);
    }

    /** SYSTEM_ADMIN 수정 */
    @Transactional
    public AdminUserResponse updateAdmin(
            Long id,
            UpdateAdminUserRequest req,
            Long adminId,
            String ip
    ) {
        // 요청자 체크
        requireSystemAdmin(adminId);

        // 타켓 체크 (삭제될 계정)
        User target = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (target.getRole() != UserRole.SYSTEM_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (req.getName() != null || req.getPhoneNumber() != null)
            target.updateMyInfo(req.getName(), req.getPhoneNumber());
        if (req.getPassword() != null)
            target.changePassword(passwordEncoder.encode(req.getPassword()));

        activityLogService.createLog(
                ActionType.UPDATE, TargetTable.USER,
                target.getId(), adminId, null, ip
        );

        return AdminUserResponse.from(target);
    }

    /** SYSTEM_ADMIN 삭제 */
    @Transactional
    public void deleteAdmin(Long id, Long adminId, String ip) {

        requireSystemAdmin(adminId);

        User target = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (target.getRole() != UserRole.SYSTEM_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // SYSTEM_ADMIN 최소 1명 삭제 방지
        long adminCount = userRepository.countByRole(UserRole.SYSTEM_ADMIN);
        if (adminCount <= 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ADMIN_MINIMUM_REQUIRED);
        }

        target.delete();

        activityLogService.createLog(
                ActionType.DELETE, TargetTable.USER,
                target.getId(), adminId, null, ip
        );
    }

    // 요청자 검증
    private void requireSystemAdmin(Long requesterId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (requester.getRole() != UserRole.SYSTEM_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
