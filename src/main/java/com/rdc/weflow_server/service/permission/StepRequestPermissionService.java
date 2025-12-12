package com.rdc.weflow_server.service.permission;

import com.rdc.weflow_server.entity.project.ProjectMember;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.entity.user.UserRole;
import com.rdc.weflow_server.entity.step.StepRequest;
import com.rdc.weflow_server.entity.step.StepRequestStatus;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.project.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StepRequestPermissionService {

    private final ProjectMemberRepository projectMemberRepository;

    /** 승인 요청 생성 권한: SYSTEM_ADMIN 또는 개발사 활성 멤버 */
    public void assertCanCreateRequest(User user, Long projectId) {
        requireUser(user);
        if (user.getRole() == UserRole.SYSTEM_ADMIN) {
            return;
        }
        requireRole(user, UserRole.AGENCY, ErrorCode.FORBIDDEN);
        requireActiveMember(projectId, user.getId(), ErrorCode.FORBIDDEN);
    }

    /** 승인 요청 수정 권한: SYSTEM_ADMIN 또는 요청자, 결정된 요청은 불가 */
    public void assertCanUpdateRequest(User user, StepRequest stepRequest) {
        requireUser(user);
        requireStepRequest(stepRequest);

        if (!stepRequest.getStatus().isEditable()) {
            throw new BusinessException(ErrorCode.STEP_REQUEST_ALREADY_DECIDED);
        }

        if (user.getRole() == UserRole.SYSTEM_ADMIN) {
            return;
        }

        if (stepRequest.getRequestedBy() != null && stepRequest.getRequestedBy().getId().equals(user.getId())) {
            return;
        }

        throw new BusinessException(ErrorCode.FORBIDDEN);
    }

    /** 승인 요청 취소 권한: SYSTEM_ADMIN 또는 요청자, REQUESTED 상태만 취소 가능 */
    public void assertCanCancelRequest(User user, StepRequest stepRequest) {
        requireUser(user);
        requireStepRequest(stepRequest);

        if (stepRequest.getStatus() != StepRequestStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.STEP_REQUEST_CANNOT_CANCEL);
        }

        if (user.getRole() == UserRole.SYSTEM_ADMIN) {
            return;
        }

        if (stepRequest.getRequestedBy() != null && stepRequest.getRequestedBy().getId().equals(user.getId())) {
            return;
        }

        throw new BusinessException(ErrorCode.FORBIDDEN);
    }

    /** 승인/반려/수정요청 권한: SYSTEM_ADMIN 또는 고객사 활성 멤버, REQUESTED 상태만 가능 */
    public void assertCanAnswerRequest(User user, StepRequest stepRequest) {
        requireUser(user);
        requireStepRequest(stepRequest);

        if (stepRequest.getStatus() != StepRequestStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.STEP_REQUEST_ALREADY_DECIDED);
        }

        if (user.getRole() == UserRole.SYSTEM_ADMIN) {
            return;
        }

        requireRole(user, UserRole.CLIENT, ErrorCode.FORBIDDEN);

        requireActiveMember(stepRequest.getStep().getProject().getId(), user.getId(), ErrorCode.FORBIDDEN);
    }

    /** 승인 요청 목록/조회 권한: SYSTEM_ADMIN 또는 해당 프로젝트 활성 멤버 */
    public void assertCanViewRequests(User user, Long projectId) {
        requireUser(user);
        if (user.getRole() == UserRole.SYSTEM_ADMIN) {
            return;
        }

        requireActiveMember(projectId, user.getId(), ErrorCode.NO_PROJECT_PERMISSION);
    }

    private void requireUser(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void requireStepRequest(StepRequest stepRequest) {
        if (stepRequest == null) {
            throw new BusinessException(ErrorCode.STEP_REQUEST_NOT_FOUND);
        }
        if (stepRequest.getStep() == null || stepRequest.getStep().getProject() == null) {
            throw new BusinessException(ErrorCode.STEP_NOT_FOUND);
        }
    }

    private void requireRole(User user, UserRole role, ErrorCode errorCode) {
        if (user.getRole() != role) {
            throw new BusinessException(errorCode);
        }
    }

    private void requireActiveMember(Long projectId, Long userId, ErrorCode errorCode) {
        ProjectMember member = projectMemberRepository.findActiveByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new BusinessException(errorCode));
        // member presence is enough; no further checks
    }
}
