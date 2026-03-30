package com.example.membersite.service;

import com.example.membersite.dto.ScheduleCreateRequest;
import com.example.membersite.dto.ScheduleResponse;
import com.example.membersite.entity.Member;
import com.example.membersite.entity.Schedule;
import com.example.membersite.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final MemberService memberService;

    public ScheduleService(ScheduleRepository scheduleRepository, MemberService memberService) {
        this.scheduleRepository = scheduleRepository;
        this.memberService = memberService;
    }

    public List<ScheduleResponse> findMonthlySchedules(String loginId, int year, int month) {
        Member member = memberService.findByLoginId(loginId);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        return scheduleRepository.findByMemberAndPlanDateBetweenOrderByPlanDateAscIdAsc(member, startDate, endDate)
                .stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    public List<ScheduleResponse> findDailySchedules(String loginId, LocalDate date) {
        Member member = memberService.findByLoginId(loginId);
        return scheduleRepository.findByMemberAndPlanDateOrderByIdAsc(member, date)
                .stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    @Transactional
    public ScheduleResponse create(String loginId, ScheduleCreateRequest request) {
        validate(request);

        Member member = memberService.findByLoginId(loginId);
        Schedule schedule = new Schedule(member, request.getDate(), request.getContent().trim());
        return ScheduleResponse.from(scheduleRepository.save(schedule));
    }

    @Transactional
    public void delete(String loginId, Long scheduleId) {
        Member member = memberService.findByLoginId(loginId);
        Schedule schedule = scheduleRepository.findByIdAndMember(scheduleId, member)
                .orElseThrow(() -> new EntityNotFoundException("일정을 찾을 수 없습니다."));
        scheduleRepository.delete(schedule);
    }

    private void validate(ScheduleCreateRequest request) {
        if (request.getDate() == null) {
            throw new IllegalArgumentException("날짜를 선택해주세요.");
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("계획 내용을 입력해주세요.");
        }

        if (request.getContent().trim().length() > 100) {
            throw new IllegalArgumentException("계획 내용은 100자 이하로 입력해주세요.");
        }
    }
}
