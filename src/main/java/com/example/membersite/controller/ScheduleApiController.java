package com.example.membersite.controller;

import com.example.membersite.dto.ScheduleCreateRequest;
import com.example.membersite.dto.ScheduleResponse;
import com.example.membersite.interceptor.LoginCheckInterceptor;
import com.example.membersite.service.ScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleApiController {

    private final ScheduleService scheduleService;

    /*
    public ScheduleApiController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }
    */

    @GetMapping
    public List<ScheduleResponse> monthlySchedules(HttpServletRequest request,
                                                   @RequestParam int year,
                                                   @RequestParam int month) {
        /*
         * previous way
         * String loginId = sessionManager.getLoginId(request);
         * if (loginId == null) {
         *     throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
         * }
         */
        String loginId = getLoginId(request);
        return scheduleService.findMonthlySchedules(loginId, year, month);
    }

    @GetMapping("/daily")
    public List<ScheduleResponse> dailySchedules(HttpServletRequest request,
                                                 @RequestParam LocalDate date) {
        /*
         * previous way
         * String loginId = sessionManager.getLoginId(request);
         * if (loginId == null) {
         *     throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
         * }
         */
        String loginId = getLoginId(request);
        return scheduleService.findDailySchedules(loginId, date);
    }

    @PostMapping
    public ResponseEntity<?> create(HttpServletRequest request,
                                    @RequestBody ScheduleCreateRequest scheduleCreateRequest) {
        /*
         * previous way
         * String loginId = sessionManager.getLoginId(request);
         * if (loginId == null) {
         *     return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
         * }
         */
        String loginId = getLoginId(request);

        try {
            return ResponseEntity.ok(scheduleService.create(loginId, scheduleCreateRequest));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @PostMapping("/{scheduleId}/delete")
    public ResponseEntity<?> delete(HttpServletRequest request,
                                    @PathVariable Long scheduleId) {
        /*
         * previous way
         * String loginId = sessionManager.getLoginId(request);
         * if (loginId == null) {
         *     return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
         * }
         */
        String loginId = getLoginId(request);

        try {
            scheduleService.delete(loginId, scheduleId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    private String getLoginId(HttpServletRequest request) {
        return (String) request.getAttribute(LoginCheckInterceptor.LOGIN_ID_ATTRIBUTE);
    }
}
