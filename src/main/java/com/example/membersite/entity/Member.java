/*
 * 회원 한 명의 데이터를 담는 객체.
 * DB의 members 테이블 한 행을 자바 객체로 표현한다.
 * 로그인 ID, 비밀번호, 이름 같은 회원 정보를 보관한다.
 */
package com.example.membersite.entity;

<<<<<<< HEAD
public class Member {

    // members 테이블 한 행을 자바 객체로 표현한다.
=======
import java.time.LocalDateTime;

public class Member {

>>>>>>> 6926320 (nointercepter)
    private Long id;
    private String loginId;
    private String password;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

<<<<<<< HEAD
    // 프레임워크가 필요로 하는 기본 생성자다.
    public Member() {
    }

    // 회원가입 입력값으로 새 회원 객체를 만들 때 사용한다.
    public Member(String loginId, String password, String name) {
=======
    public Member(Long id, String loginId, String password, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
>>>>>>> 6926320 (nointercepter)
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Member(String loginId, String password, String name) {
        this(null, loginId, password, name, null, null);
    }

    // DB에서 읽어온 회원은 id까지 포함해 복원한다.
    public Member(Long id, String loginId, String password, String name) {
        this.id = id;
        this.loginId = loginId;
        this.password = password;
        this.name = name;
    }

    // 반환: 회원 고유 ID
    public Long getId() {
        return id;
    }

    // 반환: 회원 로그인 ID
    public String getLoginId() {
        return loginId;
    }

    // 반환: 회원 비밀번호 값
    public String getPassword() {
        return password;
    }

    // 반환: 회원 이름
    public String getName() {
        return name;
    }
<<<<<<< HEAD
=======

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changePassword(String password) {
        this.password = password;
    }
>>>>>>> 6926320 (nointercepter)
}
