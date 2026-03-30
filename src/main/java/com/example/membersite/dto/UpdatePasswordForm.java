package com.example.membersite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdatePasswordForm {

    @NotBlank(message = "현재 암호를 입력하세요.")
    private String currentPassword;

    @NotBlank(message = "새 암호를 입력하세요.")
    @Size(min = 4, max = 100, message = "새 암호는 4자 이상 입력하세요.")
    private String newPassword;

    @NotBlank(message = "새 암호 확인을 입력하세요.")
    private String newPasswordConfirm;

    public boolean newPasswordMatches() {
        return newPassword != null && newPassword.equals(newPasswordConfirm);
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPasswordConfirm() {
        return newPasswordConfirm;
    }

    public void setNewPasswordConfirm(String newPasswordConfirm) {
        this.newPasswordConfirm = newPasswordConfirm;
    }
}
