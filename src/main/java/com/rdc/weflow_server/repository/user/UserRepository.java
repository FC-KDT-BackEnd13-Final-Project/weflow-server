package com.rdc.weflow_server.repository.user;

import com.rdc.weflow_server.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
