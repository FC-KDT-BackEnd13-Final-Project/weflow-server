package com.rdc.weflow_server.config.data;

import com.rdc.weflow_server.entity.company.Company;
import com.rdc.weflow_server.entity.company.CompanyStatus;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.entity.user.UserRole;
import com.rdc.weflow_server.entity.user.UserStatus;
import com.rdc.weflow_server.repository.company.CompanyRepository;
import com.rdc.weflow_server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitialDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // 이미 관리자 있으면 아무것도 안 함
        if (userRepository.existsByEmail("admin@bn-system.com")) {
            System.out.println("========================================");
            System.out.println("관리자 계정이 이미 존재합니다.");
            System.out.println("이메일: admin@bn-system.com");
            System.out.println("초기 비밀번호: admin123");
            System.out.println("========================================");
            return;
        }

        // 1. 회사 만들기
        Company company = Company.builder()
                .name("BN-SYSTEM")
                .businessNumber("787-87-01752")
                .representative("엄요한")
                .email("best@bn-system.com")
                .address("서울특별시 노원구 동일로174길 27 (공릉동, 서울창업디딤터) 303호")
                .status(CompanyStatus.ACTIVE)
                .build();
        companyRepository.save(company);

        // 2. 관리자 만들기
        User admin = User.builder()
                .email("admin@bn-system.com")
                .password(passwordEncoder.encode("admin123"))
                .name("시스템관리자")
                .phoneNumber("02-978-3140")
                .role(UserRole.SYSTEM_ADMIN)
                .status(UserStatus.ACTIVE)
                .isTemporaryPassword(false)
                .company(company)
                .build();
        userRepository.save(admin);

        System.out.println("========================================");
        System.out.println("관리자 계정 생성 완료!");
        System.out.println("이메일: admin@bn-system.com");
        System.out.println("비밀번호: admin123");
        System.out.println("========================================");
    }
}