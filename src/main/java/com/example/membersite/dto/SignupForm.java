package com.example.membersite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignupForm {

    @NotBlank(message = "로그인 아이디를 입력하세요.")
    @Size(min = 4, max = 20, message = "로그인 아이디는 4자 이상 20자 이하로 입력하세요.")
    private String loginId;

    @NotBlank(message = "암호를 입력하세요.")
    @Size(min = 4, max = 100, message = "암호는 4자 이상 입력하세요.")
    private String password;

    @NotBlank(message = "암호 확인을 입력하세요.")
    private String passwordConfirm;

    @NotBlank(message = "이름을 입력하세요.")
    @Size(max = 30, message = "이름은 30자 이하로 입력하세요.")
    private String name;

    public boolean passwordMatches() {
        return password != null && password.equals(passwordConfirm);
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
