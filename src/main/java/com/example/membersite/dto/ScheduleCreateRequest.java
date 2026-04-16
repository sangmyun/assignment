package com.example.membersite.dto;

import java.time.LocalDate;

public class ScheduleCreateRequest {

    // 프런트에서 보내는 JSON을 이 DTO가 받아 서비스로 전달한다.
    private LocalDate date;
    private String content;

    // 반환: 요청 본문에 담긴 일정 날짜
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    // 반환: 요청 본문에 담긴 일정 내용
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
