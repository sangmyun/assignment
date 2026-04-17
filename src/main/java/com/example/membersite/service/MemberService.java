/*
 * 회원 관련 비즈니스 로직을 담당하는 서비스.
 * 컨트롤러와 저장소 사이에서 회원가입, 로그인 검사, 이름/비밀번호 변경을 처리한다.
 * 저장소에서 회원을 찾은 뒤 null 검사, 비밀번호 비교 같은 실제 판단 로직을 수행한다.
 */
package com.example.membersite.service;

import com.example.membersite.dto.SignupForm;
import com.example.membersite.entity.Member;
import com.example.membersite.repository.MemberRepository;
<<<<<<< HEAD
=======
import java.util.NoSuchElementException;
>>>>>>> 6926320 (nointercepter)
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    // 서비스는 컨트롤러 요청을 받아 필요한 저장소 작업을 조합한다.
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 가입 폼을 엔티티로 바꾼 뒤 저장한다. ( 전달 받아 사용하는 것라서 따로 생성자 선언 할 필요 없음)
    public void register(SignupForm signupForm) {
        Member member = new Member(
                signupForm.getLoginId(),
                signupForm.getPassword(),
                signupForm.getName()
        );
        memberRepository.save(member);
    }

    // 반환: loginId가 이미 존재하면 true, 아니면 false
    public boolean isDuplicatedLoginId(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

    // Optional을 서비스 계층에서 해석해, 없으면 예외를 던진다.
    // 반환: 조회된 회원 객체, 없으면 예외 발생
    public Member findByLoginId(String loginId) {
<<<<<<< HEAD
        Member member = memberRepository.findByLoginId(loginId);
        if (member == null) {
            throw new IllegalArgumentException("Member not found.");
        }

        return member;
    }

    /* 현재 예제는 평문 비밀번호를 단순 비교한다.*/
    // 반환: 로그인 ID와 비밀번호가 일치하면 true, 아니면 false
    public boolean authenticate(String loginId, String password) {
        //Member(
        //    id=1,
        //    loginId="admin",
        //    password="1234",
        //    name="관리자"
        //)
        Member member = memberRepository.findByLoginId(loginId);
        if (member == null) {
            return false;
        }

        return password.equals(member.getPassword());
=======
        return memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다."));
    }

    public Member login(String loginId, String password) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));
        if (!member.getPassword().equals(password)) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        return member;
>>>>>>> 6926320 (nointercepter)
    }

    public void updateName(String loginId, String name) {
<<<<<<< HEAD
        Member member = findByLoginId(loginId);
        memberRepository.updateName(member.getId(), name);
=======
        memberRepository.updateName(loginId, name);
>>>>>>> 6926320 (nointercepter)
    }

    // 반환: 현재 비밀번호가 회원의 비밀번호와 일치하면 true, 아니면 false
    public boolean matchesPassword(String loginId, String rawPassword) {
        Member member = findByLoginId(loginId);
<<<<<<< HEAD
        return rawPassword.equals(member.getPassword());
=======
        return member.getPassword().equals(rawPassword);
>>>>>>> 6926320 (nointercepter)
    }

    public void updatePassword(String loginId, String newPassword) {
<<<<<<< HEAD
        Member member = findByLoginId(loginId);
        memberRepository.updatePassword(member.getId(), newPassword);
=======
        memberRepository.updatePassword(loginId, newPassword);
>>>>>>> 6926320 (nointercepter)
    }
}
