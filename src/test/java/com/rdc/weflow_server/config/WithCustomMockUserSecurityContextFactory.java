package com.rdc.weflow_server.config;

import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.entity.company.Company;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.entity.user.UserRole;
import com.rdc.weflow_server.entity.user.UserStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithCustomMockUserSecurityContextFactory implements WithSecurityContextFactory<WithCustomMockUser> {

    @Override
    public SecurityContext createSecurityContext(WithCustomMockUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // Mock Company
        Company company = Company.builder()
                .name("Test Company")
                .businessNumber("123-45-67890")
                .representative("Test Rep")
                .email("test@company.com")
                .address("Test Address")
                .build();

        // Mock User
        User user = User.builder()
                .id(annotation.id())
                .email(annotation.email())
                .name(annotation.name())
                .password("encoded-password")
                .phoneNumber("010-0000-0000")
                .role(UserRole.valueOf(annotation.role()))
                .status(UserStatus.ACTIVE)
                .company(company)
                .build();

        CustomUserDetails principal = new CustomUserDetails(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        context.setAuthentication(auth);

        return context;
    }
}
