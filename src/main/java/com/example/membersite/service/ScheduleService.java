package com.example.membersite.service;

import com.example.membersite.dto.ScheduleCreateRequest;
import com.example.membersite.dto.ScheduleReorderRequest;
import com.example.membersite.dto.ScheduleResponse;
import com.example.membersite.entity.Member;
import com.example.membersite.entity.Schedule;
import com.example.membersite.repository.ScheduleRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .findByMemberIdAndPlanDateBetweenOrderByPlanDateAscDisplayOrderAscIdAsc(
                        member.getId(),
                        startDate,
                        endDate
                );
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
        List<Schedule> schedules = scheduleRepository
                .findByMemberIdAndPlanDateOrderByDisplayOrderAscIdAsc(member.getId(), date);
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
        int nextDisplayOrder = scheduleRepository.findMaxDisplayOrderByMemberIdAndPlanDate(
                member.getId(),
                request.getDate()
        ) + 1;
        Schedule schedule = new Schedule(member.getId(), request.getDate(), request.getContent().trim(), nextDisplayOrder);
        return ScheduleResponse.from(scheduleRepository.save(schedule));
    }

    /**
     * Reorders all schedules of a date for the authenticated user in one transaction.
     *
     * @param loginId login id
     * @param request reorder request
     */
    @Transactional
    public void reorder(String loginId, ScheduleReorderRequest request) {
        validateReorderRequest(request);

        Member member = memberService.findByLoginId(loginId);
        List<Schedule> schedules = scheduleRepository
                .findByMemberIdAndPlanDateOrderByDisplayOrderAscIdAsc(member.getId(), request.getDate());

        if (schedules.size() != request.getScheduleIds().size()) {
            throw new IllegalArgumentException("Reorder payload must contain all schedules of the selected date.");
        }

        Set<Long> existingIds = new HashSet<>();
        for (Schedule schedule : schedules) {
            existingIds.add(schedule.getId());
        }

        for (Long scheduleId : request.getScheduleIds()) {
            if (!existingIds.contains(scheduleId)) {
                throw new IllegalArgumentException("Reorder payload contains invalid schedule id.");
            }
        }

        for (int index = 0; index < request.getScheduleIds().size(); index++) {
            Long scheduleId = request.getScheduleIds().get(index);
            int affectedRows = scheduleRepository.updateDisplayOrderByIdAndMemberId(
                    scheduleId,
                    member.getId(),
                    index + 1
            );
            if (affectedRows != 1) {
                throw new IllegalStateException("Failed to reorder schedules.");
            }
        }
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

    /**
     * Validates schedule reorder input.
     *
     * @param request reorder request
     */
    private void validateReorderRequest(ScheduleReorderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Reorder request is required.");
        }

        if (request.getDate() == null) {
            throw new IllegalArgumentException("Date is required.");
        }

        if (request.getScheduleIds() == null || request.getScheduleIds().isEmpty()) {
            throw new IllegalArgumentException("Schedule id list is required.");
        }

        Set<Long> uniqueIds = new HashSet<>();
        for (Long scheduleId : request.getScheduleIds()) {
            if (scheduleId == null) {
                throw new IllegalArgumentException("Schedule id must not be null.");
            }
            if (!uniqueIds.add(scheduleId)) {
                throw new IllegalArgumentException("Schedule id list must not contain duplicates.");
            }
        }
    }
}
