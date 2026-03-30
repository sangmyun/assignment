package com.example.membersite.service;

import com.example.membersite.dto.SignupForm;
import com.example.membersite.entity.Member;
import com.example.membersite.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(SignupForm signupForm) {
        Member member = new Member(
                signupForm.getLoginId(),
                passwordEncoder.encode(signupForm.getPassword()),
                signupForm.getName()
        );
        memberRepository.save(member);
    }

    public boolean isDuplicatedLoginId(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

    public Member findByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
    }

    @Transactional
    public void updateName(String loginId, String name) {
        Member member = findByLoginId(loginId);
        member.changeName(name);
    }

    public boolean matchesPassword(String loginId, String rawPassword) {
        Member member = findByLoginId(loginId);
        return passwordEncoder.matches(rawPassword, member.getPassword());
    }

    @Transactional
    public void updatePassword(String loginId, String newPassword) {
        Member member = findByLoginId(loginId);
        member.changePassword(passwordEncoder.encode(newPassword));
    }
}
