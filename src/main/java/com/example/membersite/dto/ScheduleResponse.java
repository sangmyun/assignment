package com.example.membersite.dto;

import com.example.membersite.entity.Schedule;
import java.time.LocalDate;

public class ScheduleResponse {

    private final Long id;
    private final LocalDate date;
    private final String content;

    public ScheduleResponse(Long id, LocalDate date, String content) {
        this.id = id;
        this.date = date;
        this.content = content;
    }

    public static ScheduleResponse from(Schedule schedule) {
        return new ScheduleResponse(schedule.getId(), schedule.getPlanDate(), schedule.getContent());
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }
}
