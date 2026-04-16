package com.example.membersite.dto;

import com.example.membersite.entity.Schedule;
import java.time.LocalDate;

public class ScheduleResponse {

    // 엔티티 전체를 노출하지 않고, 화면에 필요한 값만 API 응답으로 보낸다.
    private final Long id;
    private final LocalDate date;
    private final String content;

    public ScheduleResponse(Long id, LocalDate date, String content) {
        this.id = id;
        this.date = date;
        this.content = content;
    }

    // 반환: Schedule 엔티티를 API 응답용 DTO로 바꾼 객체
    public static ScheduleResponse from(Schedule schedule) {
        return new ScheduleResponse(schedule.getId(), schedule.getPlanDate(), schedule.getContent());
    }

    // 반환: 일정 ID
    public Long getId() {
        return id;
    }

    // 반환: 일정 날짜
    public LocalDate getDate() {
        return date;
    }

    // 반환: 일정 내용
    public String getContent() {
        return content;
    }
}
