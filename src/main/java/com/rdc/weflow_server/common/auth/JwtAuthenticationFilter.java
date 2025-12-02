package com.rdc.weflow_server.common.auth;

import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.repository.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. 요청 헤더에서 JWT 토큰 추출
            String token = getTokenFromRequest(request);

            // 2. 토큰이 있고 유효한지 확인
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {

                // 3. 토큰에서 이메일 추출
                String email = jwtTokenProvider.getEmail(token);

                // 4. DB에서 사용자 조회 (삭제된 사용자 제외)
                User user = userRepository.findByEmail(email)
                        .filter(u -> u.getDeletedAt() == null)
                        .orElse(null);

                if (user != null) {
                    // 5. User 엔티티를 CustomUserDetails로 감싸기
                    CustomUserDetails userDetails = new CustomUserDetails(user);

                    // 6. Spring Security 인증 객체 생성
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // 7. 요청 정보를 인증 객체에 추가
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // 8. SecurityContext에 인증 정보 저장 (이제 전역에서 사용 가능)
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("인증 성공: {}", email);
                }
            }
        } catch (Exception e) {
            log.error("인증 처리 중 오류 발생", e);
        }

        // 9. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * 요청 헤더에서 Bearer 토큰 추출
     * Authorization: Bearer {token}
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer " 제거
        }

        return null;
    }
}