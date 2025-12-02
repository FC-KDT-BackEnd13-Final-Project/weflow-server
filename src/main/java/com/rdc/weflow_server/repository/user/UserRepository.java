package com.rdc.weflow_server.repository.user;

import com.rdc.weflow_server.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 찾기 (로그인, 중복 체크용)
    Optional<User> findByEmail(String email);

    // 이메일 중복 확인
    boolean existsByEmail(String email);

    // 삭제되지 않은 회원만 조회 (관리자 회원 목록용)
    List<User> findAllByDeletedAtIsNull();
}