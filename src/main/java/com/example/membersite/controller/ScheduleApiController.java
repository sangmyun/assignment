package com.example.membersite.controller;

import com.example.membersite.dto.ScheduleCreateRequest;
import com.example.membersite.dto.ScheduleResponse;
import com.example.membersite.service.ScheduleService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleApiController {

    private final ScheduleService scheduleService;

    public ScheduleApiController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public List<ScheduleResponse> monthlySchedules(@AuthenticationPrincipal UserDetails userDetails,
                                                   @RequestParam int year,
                                                   @RequestParam int month) {
        return scheduleService.findMonthlySchedules(userDetails.getUsername(), year, month);
    }

    @GetMapping("/daily")
    public List<ScheduleResponse> dailySchedules(@AuthenticationPrincipal UserDetails userDetails,
                                                 @RequestParam LocalDate date) {
        return scheduleService.findDailySchedules(userDetails.getUsername(), date);
    }

    @PostMapping
    public ResponseEntity<?> create(@AuthenticationPrincipal UserDetails userDetails,
                                    @RequestBody ScheduleCreateRequest request) {
        try {
            return ResponseEntity.ok(scheduleService.create(userDetails.getUsername(), request));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        }
    }

    @PostMapping("/{scheduleId}/delete")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails userDetails,
                                       @PathVariable Long scheduleId) {
        scheduleService.delete(userDetails.getUsername(), scheduleId);
        return ResponseEntity.ok().build();
    }
}
