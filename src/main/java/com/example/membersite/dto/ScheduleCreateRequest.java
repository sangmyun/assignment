package com.example.membersite.dto;

import java.time.LocalDate;

public class ScheduleCreateRequest {

    private LocalDate date;
    private String content;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
