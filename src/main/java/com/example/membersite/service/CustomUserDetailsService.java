package com.example.membersite.service;

import com.example.membersite.entity.Member;
import com.example.membersite.repository.MemberRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public CustomUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다."));

        return User.builder()
                .username(member.getLoginId())
                .password(member.getPassword())
                .roles("USER")
                .build();
    }
}
