package com.rdc.weflow_server.service.permission;

import com.rdc.weflow_server.entity.project.ProjectMember;
import com.rdc.weflow_server.entity.project.ProjectRole;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.entity.user.UserRole;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.project.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StepPermissionService {

    private final ProjectMemberRepository projectMemberRepository;

    public void assertCanManageSteps(User user, Long projectId) {
        validateUser(user);
        if (user.getRole() == UserRole.SYSTEM_ADMIN) {
            return;
        }

        ProjectMember member = projectMemberRepository.findActiveByProjectIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));

        if (member.getUser().getRole() != UserRole.AGENCY || member.getRole() != ProjectRole.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    public void assertCanViewProject(User user, Long projectId) {
        validateUser(user);
        if (user.getRole() == UserRole.SYSTEM_ADMIN) {
            return;
        }

        boolean isMember = projectMemberRepository.findActiveByProjectIdAndUserId(projectId, user.getId()).isPresent();
        if (!isMember) {
            throw new BusinessException(ErrorCode.NO_PROJECT_PERMISSION);
        }
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
