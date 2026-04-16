package com.example.membersite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdatePasswordForm {

    @NotBlank(message = "현재 비밀번호를 입력하세요.")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호를 입력하세요.")
    @Size(min = 4, max = 100, message = "새 비밀번호는 4자 이상 입력하세요.")
    private String newPassword;

    @NotBlank(message = "새 비밀번호 확인을 입력하세요.")
    private String newPasswordConfirm;

    // 반환: 새 비밀번호와 새 비밀번호 확인 값이 같으면 true, 아니면 false
    public boolean newPasswordMatches() {
        return newPassword != null && newPassword.equals(newPasswordConfirm);
    }

    // 반환: 입력된 현재 비밀번호 값
    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    // 반환: 입력된 새 비밀번호 값
    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    // 반환: 입력된 새 비밀번호 확인 값
    public String getNewPasswordConfirm() {
        return newPasswordConfirm;
    }

    public void setNewPasswordConfirm(String newPasswordConfirm) {
        this.newPasswordConfirm = newPasswordConfirm;
    }
}
