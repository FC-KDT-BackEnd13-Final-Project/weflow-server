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

    public void assertCanCreateRequest(User user, Long projectId) {
        validateUser(user);
        if (user.getRole() == UserRole.SYSTEM_ADMIN) {
            return;
        }
        if (user.getRole() != UserRole.AGENCY) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        requireActiveMember(projectId, user.getId(), ErrorCode.FORBIDDEN);
    }

    public void assertCanUpdateRequest(User user, StepRequest stepRequest) {
        validateUser(user);
        validateStepRequest(stepRequest);

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

    public void assertCanCancelRequest(User user, StepRequest stepRequest) {
        validateUser(user);
        validateStepRequest(stepRequest);

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

    public void assertCanAnswerRequest(User user, StepRequest stepRequest) {
        validateUser(user);
        validateStepRequest(stepRequest);

        if (stepRequest.getStatus() != StepRequestStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.STEP_REQUEST_ALREADY_DECIDED);
        }

        if (user.getRole() == UserRole.SYSTEM_ADMIN) {
            return;
        }

        if (user.getRole() != UserRole.CLIENT) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        requireActiveMember(stepRequest.getStep().getProject().getId(), user.getId(), ErrorCode.FORBIDDEN);
    }

    public void assertCanViewRequests(User user, Long projectId) {
        validateUser(user);
        if (user.getRole() == UserRole.SYSTEM_ADMIN) {
            return;
        }

        requireActiveMember(projectId, user.getId(), ErrorCode.NO_PROJECT_PERMISSION);
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateStepRequest(StepRequest stepRequest) {
        if (stepRequest == null) {
            throw new BusinessException(ErrorCode.STEP_REQUEST_NOT_FOUND);
        }
        if (stepRequest.getStep() == null || stepRequest.getStep().getProject() == null) {
            throw new BusinessException(ErrorCode.STEP_NOT_FOUND);
        }
    }

    private void requireActiveMember(Long projectId, Long userId, ErrorCode errorCode) {
        ProjectMember member = projectMemberRepository.findActiveByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new BusinessException(errorCode));
        // member presence is enough; no further checks
    }
}
