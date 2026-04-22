/*
 * 회원 한 명의 데이터를 담는 객체.
 * DB의 members 테이블 한 행을 자바 객체로 표현한다.
 * 로그인 ID, 비밀번호, 이름 같은 회원 정보를 보관한다.
 */
package com.example.membersite.entity;

public class Member {

    // members 테이블 한 행을 자바 객체로 표현한다.
    private Long id;
    private String loginId;
    private String password;
    private String name;

    // 프레임워크가 필요로 하는 기본 생성자다.
    public Member() {
    }

    // 회원가입 입력값으로 새 회원 객체를 만들 때 사용한다.
    public Member(String loginId, String password, String name) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
    }

    // DB에서 읽어온 회원은 id까지 포함해 복원한다.
    public Member(Long id, String loginId, String password, String name) {
        this.id = id;
        this.loginId = loginId;
        this.password = password;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getLoginId() {return loginId;}

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }
}
