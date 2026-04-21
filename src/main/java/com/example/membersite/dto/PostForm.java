package com.example.membersite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PostForm {

    @NotBlank(message = "제목을 입력하세요.")
    @Size(max = 100, message = "제목은 100자 이하로 입력하세요.")
    private String title;

    @NotBlank(message = "내용을 입력하세요.")
    @Size(max = 5000, message = "내용은 5000자 이하로 입력하세요.")
    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
