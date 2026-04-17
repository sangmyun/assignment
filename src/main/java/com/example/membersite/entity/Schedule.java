/*
 * 일정 한 건의 데이터를 담는 객체.
 * DB의 schedules 테이블 한 행을 자바 객체로 표현한다.
 * 어떤 회원의 일정인지, 날짜와 내용이 무엇인지 저장한다.
 */
package com.example.membersite.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Schedule {

<<<<<<< HEAD
    // schedules 테이블 한 행을 자바 객체로 표현한다.
    private Long id;
    private Long memberId;
=======
    private Long id;
    private Long memberId;
    private Member member;
>>>>>>> 6926320 (nointercepter)
    private LocalDate planDate;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

<<<<<<< HEAD
    // 프레임워크가 필요로 하는 기본 생성자다.
    public Schedule() {
    }

    // 새 일정 생성 시에는 생성 시각을 현재 시각으로 자동 기록한다.
    public Schedule(Long memberId, LocalDate planDate, String content) {
        this.memberId = memberId;
=======
    public Schedule(Long id, Long memberId, Member member, LocalDate planDate, String content,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.memberId = memberId;
        this.member = member;
>>>>>>> 6926320 (nointercepter)
        this.planDate = planDate;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Schedule(Member member, LocalDate planDate, String content) {
        this(null, member.getId(), member, planDate, content, LocalDateTime.now(), null);
    }

    // DB에서 읽어온 일정은 id와 생성 시각까지 포함해 복원한다.
    public Schedule(Long id, Long memberId, LocalDate planDate, String content, LocalDateTime createdAt) {
        this.id = id;
        this.memberId = memberId;
        this.planDate = planDate;
        this.content = content;
        this.createdAt = createdAt;
    }

    // 반환: 일정 고유 ID
    public Long getId() {
        return id;
    }

<<<<<<< HEAD
    // 반환: 일정을 소유한 회원 ID
    public Long getMemberId() {
        return memberId;
=======
    public Long getMemberId() {
        return memberId;
    }

    public Member getMember() {
        return member;
>>>>>>> 6926320 (nointercepter)
    }

    // 반환: 일정 날짜
    public LocalDate getPlanDate() {
        return planDate;
    }

    // 반환: 일정 내용
    public String getContent() {
        return content;
    }

    // 반환: 일정 생성 시각
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
