package com.example.membersite.service;

import com.example.membersite.dto.SignupForm;
import com.example.membersite.entity.Member;
import com.example.membersite.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /*
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }
    */

    // Save password as hash, never as plain text.
    public void register(SignupForm signupForm) {
        String hashedPassword = passwordEncoder.encode(signupForm.getPassword());
        Member member = new Member(
                signupForm.getLoginId(),
                hashedPassword,
                signupForm.getName()
        );
        memberRepository.save(member);
    }

    public boolean isDuplicatedLoginId(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

    public Member  findByLoginId(String loginId) {
        Member member = memberRepository.findByLoginId(loginId);
        if (member == null) {
            throw new IllegalArgumentException("Member not found.");
        }

        return member;
    }

    public boolean authenticate(String loginId, String password) {
        Member member = memberRepository.findByLoginId(loginId);
        if (member == null) {
            return false;
        }

        /* return password.equals(member.getPassword());*/
        return passwordEncoder.matches(password, member.getPassword());
    }

    public void updateName(String loginId, String name) {
        Member member = findByLoginId(loginId);
        memberRepository.updateName(member.getId(), name);
    }

    public boolean matchesPassword(String loginId, String rawPassword) {
        Member member = findByLoginId(loginId);
        /* return rawPassword.equals(member.getPassword());*/
        return passwordEncoder.matches(rawPassword, member.getPassword());
    }

    public void updatePassword(String loginId, String newPassword) {
        Member member = findByLoginId(loginId);
        /* memberRepository.updatePassword(member.getId(), newPassword);*/
        String hashedPassword = passwordEncoder.encode(newPassword);
        memberRepository.updatePassword(member.getId(), hashedPassword);
    }
}
