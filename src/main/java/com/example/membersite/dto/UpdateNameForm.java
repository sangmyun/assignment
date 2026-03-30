package com.example.membersite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateNameForm {

    @NotBlank(message = "이름을 입력하세요.")
    @Size(max = 30, message = "이름은 30자 이하로 입력하세요.")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
