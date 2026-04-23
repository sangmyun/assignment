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

    /**
     * Registers a new member with a hashed password.
     *
     * @param signupForm sign-up input
     */
    public void register(SignupForm signupForm) {
        String hashedPassword = passwordEncoder.encode(signupForm.getPassword());
        Member member = new Member(
                signupForm.getLoginId(),
                hashedPassword,
                signupForm.getName()
        );
        memberRepository.save(member);
    }

    /**
     * Checks whether a login id is already taken.
     *
     * @param loginId login id
     * @return true when duplicated
     */
    public boolean isDuplicatedLoginId(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

    /**
     * Finds a member by login id.
     *
     * @param loginId login id
     * @return member entity
     * @throws IllegalArgumentException when member is not found
     */
    public Member findByLoginId(String loginId) {
        Member member = memberRepository.findByLoginId(loginId);
        if (member == null) {
            throw new IllegalArgumentException("Member not found.");
        }

        return member;
    }

    /**
     * Authenticates a login id and password.
     *
     * @param loginId login id
     * @param password raw password
     * @return true when authentication succeeds
     */
    public boolean authenticate(String loginId, String password) {
        Member member = memberRepository.findByLoginId(loginId);
        if (member == null) {
            return false;
        }

        return passwordEncoder.matches(password, member.getPassword());
    }

    /**
     * Updates member display name.
     *
     * @param loginId login id
     * @param name new name
     */
    public void updateName(String loginId, String name) {
        Member member = findByLoginId(loginId);
        memberRepository.updateName(member.getId(), name);
    }

    /**
     * Checks whether raw password matches the stored hash.
     *
     * @param loginId login id
     * @param rawPassword raw password
     * @return true when password matches
     */
    public boolean matchesPassword(String loginId, String rawPassword) {
        Member member = findByLoginId(loginId);
        return passwordEncoder.matches(rawPassword, member.getPassword());
    }

    /**
     * Updates member password after hashing.
     *
     * @param loginId login id
     * @param newPassword new raw password
     */
    public void updatePassword(String loginId, String newPassword) {
        Member member = findByLoginId(loginId);
        String hashedPassword = passwordEncoder.encode(newPassword);
        memberRepository.updatePassword(member.getId(), hashedPassword);
    }
}
