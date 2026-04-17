/*
 * 일정 조회/등록/삭제를 처리하는 REST API 컨트롤러.
 * 먼저 세션 쿠키로 현재 로그인한 사용자의 loginId를 찾는다.
 * 인증이 끝나면 ScheduleService를 호출해서 일정 데이터를 JSON으로 반환하거나 수정한다.
 */
package com.example.membersite.controller;

import com.example.membersite.config.SessionConst;
import com.example.membersite.dto.ScheduleCreateRequest;
import com.example.membersite.dto.ScheduleResponse;
import com.example.membersite.service.ScheduleService;
<<<<<<< HEAD
import com.example.membersite.support.SessionManager;
import jakarta.servlet.http.HttpServletRequest;
=======
import jakarta.servlet.http.HttpSession;
>>>>>>> 6926320 (nointercepter)
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleApiController {

    private final ScheduleService scheduleService;
    private final SessionManager sessionManager;

    public ScheduleApiController(ScheduleService scheduleService, SessionManager sessionManager) {
        this.scheduleService = scheduleService;
        this.sessionManager = sessionManager;
    }

    // @RestController는 반환값을 뷰 이름이 아니라 JSON 응답 본문으로 처리한다.
    // 반환: 로그인 사용자의 월별 일정 목록 JSON
    @GetMapping
<<<<<<< HEAD
    public List<ScheduleResponse> monthlySchedules(HttpServletRequest request,
                                                   @RequestParam int year,
                                                   @RequestParam int month) {
        String loginId = sessionManager.getLoginId(request);
        if (loginId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return scheduleService.findMonthlySchedules(loginId, year, month);
=======
    public List<ScheduleResponse> monthlySchedules(HttpSession session,
                                                   @RequestParam int year,
                                                   @RequestParam int month) {
        return scheduleService.findMonthlySchedules(loginId(session), year, month);
>>>>>>> 6926320 (nointercepter)
    }

    // 반환: 로그인 사용자의 일별 일정 목록 JSON
    @GetMapping("/daily")
<<<<<<< HEAD
    public List<ScheduleResponse> dailySchedules(HttpServletRequest request,
                                                 @RequestParam LocalDate date) {
        String loginId = sessionManager.getLoginId(request);
        if (loginId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return scheduleService.findDailySchedules(loginId, date);
=======
    public List<ScheduleResponse> dailySchedules(HttpSession session, @RequestParam LocalDate date) {
        return scheduleService.findDailySchedules(loginId(session), date);
>>>>>>> 6926320 (nointercepter)
    }

    // RequestBody는 JSON 요청 본문을 DTO로 변환한다.
    // 반환: 생성된 일정 또는 에러 상태를 담은 HTTP 응답
    @PostMapping
<<<<<<< HEAD
    public ResponseEntity<?> create(HttpServletRequest request,
                                    @RequestBody ScheduleCreateRequest scheduleCreateRequest) {
        String loginId = sessionManager.getLoginId(request);
        if (loginId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            return ResponseEntity.ok(scheduleService.create(loginId, scheduleCreateRequest));
=======
    public ResponseEntity<?> create(HttpSession session, @RequestBody ScheduleCreateRequest request) {
        try {
            return ResponseEntity.ok(scheduleService.create(loginId(session), request));
>>>>>>> 6926320 (nointercepter)
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        }
    }

    // 반환: 삭제 성공 또는 실패 상태를 담은 HTTP 응답
    @PostMapping("/{scheduleId}/delete")
<<<<<<< HEAD
    public ResponseEntity<?> delete(HttpServletRequest request,
                                    @PathVariable Long scheduleId) {
        String loginId = sessionManager.getLoginId(request);
        if (loginId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            scheduleService.delete(loginId, scheduleId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
=======
    public ResponseEntity<Void> delete(HttpSession session, @PathVariable Long scheduleId) {
        scheduleService.delete(loginId(session), scheduleId);
        return ResponseEntity.ok().build();
>>>>>>> 6926320 (nointercepter)
    }

    private String loginId(HttpSession session) {
        return (String) session.getAttribute(SessionConst.LOGIN_MEMBER);
    }
}
