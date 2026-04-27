package com.example.membersite.controller;

import com.example.membersite.dto.ScheduleCreateRequest;
import com.example.membersite.dto.ScheduleReorderRequest;
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

    /**
     * Returns schedules for a specific month.
     *
     * @param request servlet request
     * @param year target year
     * @param month target month
     * @return monthly schedule list
     */
    @GetMapping
    public List<ScheduleResponse> monthlySchedules(HttpServletRequest request,
                                                   @RequestParam int year,
                                                   @RequestParam int month) {
        String loginId = getLoginId(request);
        return scheduleService.findMonthlySchedules(loginId, year, month);
    }

    /**
     * Returns schedules for a specific day.
     *
     * @param request servlet request
     * @param date target date
     * @return daily schedule list
     */
    @GetMapping("/daily")
    public List<ScheduleResponse> dailySchedules(HttpServletRequest request,
                                                 @RequestParam LocalDate date) {
        String loginId = getLoginId(request);
        return scheduleService.findDailySchedules(loginId, date);
    }

    /**
     * Creates a schedule.
     *
     * @param request servlet request
     * @param scheduleCreateRequest create payload
     * @return 200 with created schedule or 400 with validation message
     */
    @PostMapping
    public ResponseEntity<?> create(HttpServletRequest request,
                                    @RequestBody ScheduleCreateRequest scheduleCreateRequest) {
        String loginId = getLoginId(request);

        try {
            return ResponseEntity.ok(scheduleService.create(loginId, scheduleCreateRequest));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    /**
     * Reorders schedules of a selected date.
     *
     * @param request servlet request
     * @param reorderRequest reorder payload
     * @return 200 on success or 400 with validation message
     */
    @PostMapping("/reorder")
    public ResponseEntity<?> reorder(HttpServletRequest request,
                                     @RequestBody ScheduleReorderRequest reorderRequest) {
        String loginId = getLoginId(request);

        try {
            scheduleService.reorder(loginId, reorderRequest);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    /**
     * Deletes a schedule by id.
     *
     * @param request servlet request
     * @param scheduleId schedule id
     * @return 200 on success or 404 when not found
     */
    @PostMapping("/{scheduleId}/delete")
    public ResponseEntity<?> delete(HttpServletRequest request,
                                    @PathVariable Long scheduleId) {
        String loginId = getLoginId(request);

        try {
            scheduleService.delete(loginId, scheduleId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Reads login id attached by the login-check interceptor.
     *
     * @param request servlet request
     * @return authenticated login id
     */
    private String getLoginId(HttpServletRequest request) {
        return (String) request.getAttribute(LoginCheckInterceptor.LOGIN_ID_ATTRIBUTE);
    }
}
