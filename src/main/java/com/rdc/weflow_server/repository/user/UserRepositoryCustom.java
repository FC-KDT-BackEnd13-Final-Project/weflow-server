package com.rdc.weflow_server.repository.user;

import com.rdc.weflow_server.dto.user.request.UserSearchCondition;
import com.rdc.weflow_server.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserRepositoryCustom {
    Page<User> searchUsers(UserSearchCondition condition, Pageable pageable);

    // 이메일 리스트로 중복된 이메일만 추출
    List<String> findExistingEmails(List<String> emails);
}