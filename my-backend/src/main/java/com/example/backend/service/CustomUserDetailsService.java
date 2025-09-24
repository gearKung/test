package com.example.backend.service;

import com.example.backend.HotelOwner.domain.User;
import com.example.backend.repository.LoginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final LoginRepository loginRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = loginRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 가진 사용자를 찾을 수 없습니다: " + email));

        // Spring Security가 이해할 수 있는 UserDetails 형태로 반환합니다.
        // 여기서는 간단하게 권한 없이 이메일과 패스워드만 사용합니다.
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword() != null ? user.getPassword() : "", // 소셜 로그인의 경우 비밀번호가 없을 수 있음
                Collections.emptyList() // 권한 설정 (필요 시 확장)
        );
    }
}