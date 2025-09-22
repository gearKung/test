package com.example.backend.config;

import com.example.backend.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ✨ CORS 설정을 가장 먼저 적용합니다.
                .cors(withDefaults())
                // ✨ Stateless API를 위한 CSRF, HTTP Basic, Form Login 비활성화
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                // ✨ 세션을 사용하지 않도록 설정 (JWT 인증의 핵심)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http
                // ✨ URL 경로별 접근 권한 설정
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                            "/api/users/register",
                            "/api/users/login",
                            "/api/password/**",
                            "/oauth2/**",
                            "/login/oauth2/code/**",
                            // 소셜 로그인 성공 후 리다이렉트되는 경로 허용
                            "/auth/oauth2/success"
                        ).permitAll()
                        // ✨ /api/** 로 시작하는 모든 경로는 인증을 요구합니다.
                        // .requestMatchers("/api/hotels/**").hasRole("BUSINESS") // 나중에 업주만으로 수정
                        .requestMatchers("/api/**").authenticated()
                        // 그 외 모든 경로는 일단 허용
                        .anyRequest().permitAll()
                );
        
        http
                // ✨ 소셜 로그인(OAuth2) 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                );

        http
                // ✨ 모든 요청 전에 직접 만든 JwtAuthenticationFilter를 실행하여 토큰을 검증합니다.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}