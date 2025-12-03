package com.rdc.weflow_server.repository.user;

import com.rdc.weflow_server.dto.user.request.UserSearchCondition;
import com.rdc.weflow_server.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    Page<User> searchUsers(UserSearchCondition condition, Pageable pageable);
}