package com.example.membersite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignupForm {

    // 검증 애너테이션은 컨트롤러의 @Valid와 함께 동작한다.
    @NotBlank(message = "로그인 아이디를 입력하세요.")
    @Size(min = 4, max = 20, message = "로그인 아이디는 4자 이상 20자 이하로 입력하세요.")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Size(min = 4, max = 100, message = "비밀번호는 4자 이상 입력하세요.")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력하세요.")
    private String passwordConfirm;

    @NotBlank(message = "이름을 입력하세요.")
    @Size(max = 30, message = "이름은 30자 이하로 입력하세요.")
    private String name;

    // 두 비밀번호 입력값이 같은지 확인하는 보조 메서드다.
    // 반환: 비밀번호와 비밀번호 확인 값이 같으면 true, 아니면 false
    public boolean passwordMatches() {
        return password != null && password.equals(passwordConfirm);
    }

    // 반환: 입력된 로그인 ID 값
    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    // 반환: 입력된 비밀번호 값
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // 반환: 입력된 비밀번호 확인 값
    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    // 반환: 입력된 회원 이름 값
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
