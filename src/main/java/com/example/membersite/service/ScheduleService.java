package com.example.membersite.service;

import com.example.membersite.dto.ScheduleCreateRequest;
import com.example.membersite.dto.ScheduleResponse;
import com.example.membersite.entity.Member;
import com.example.membersite.entity.Schedule;
import com.example.membersite.repository.ScheduleRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final MemberService memberService;

    /**
     * Finds schedules in the given month for the authenticated user.
     *
     * @param loginId login id
     * @param year year
     * @param month month
     * @return monthly schedule responses
     */
    public List<ScheduleResponse> findMonthlySchedules(String loginId, int year, int month) {
        Member member = memberService.findByLoginId(loginId);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Schedule> schedules = scheduleRepository
                .findByMemberIdAndPlanDateBetweenOrderByPlanDateAscIdAsc(member.getId(), startDate, endDate);
        List<ScheduleResponse> responses = new ArrayList<>();
        for (Schedule schedule : schedules) {
            responses.add(ScheduleResponse.from(schedule));
        }
        return responses;
    }

    /**
     * Finds schedules of a specific date for the authenticated user.
     *
     * @param loginId login id
     * @param date target date
     * @return daily schedule responses
     */
    public List<ScheduleResponse> findDailySchedules(String loginId, LocalDate date) {
        Member member = memberService.findByLoginId(loginId);
        List<Schedule> schedules = scheduleRepository.findByMemberIdAndPlanDateOrderByIdAsc(member.getId(), date);
        List<ScheduleResponse> responses = new ArrayList<>();
        for (Schedule schedule : schedules) {
            responses.add(ScheduleResponse.from(schedule));
        }
        return responses;
    }

    /**
     * Creates a schedule for the authenticated user.
     *
     * @param loginId login id
     * @param request create request
     * @return created schedule response
     */
    public ScheduleResponse create(String loginId, ScheduleCreateRequest request) {
        validate(request);

        Member member = memberService.findByLoginId(loginId);
        Schedule schedule = new Schedule(member.getId(), request.getDate(), request.getContent().trim());
        return ScheduleResponse.from(scheduleRepository.save(schedule));
    }

    /**
     * Deletes a schedule owned by the authenticated user.
     *
     * @param loginId login id
     * @param scheduleId schedule id
     * @throws IllegalArgumentException when schedule is not found
     */
    public void delete(String loginId, Long scheduleId) {
        Member member = memberService.findByLoginId(loginId);
        Schedule schedule = scheduleRepository.findByIdAndMemberId(scheduleId, member.getId());
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule not found.");
        }
        scheduleRepository.deleteById(schedule.getId());
    }

    /**
     * Validates schedule create input.
     *
     * @param request create request
     */
    private void validate(ScheduleCreateRequest request) {
        if (request.getDate() == null) {
            throw new IllegalArgumentException("Date is required.");
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Content is required.");
        }

        if (request.getContent().trim().length() > 100) {
            throw new IllegalArgumentException("Content must be 100 characters or less.");
        }
    }
}
