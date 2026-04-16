/*
 * 일정 관련 비즈니스 로직을 담당하는 서비스.
 * 로그인한 사용자의 회원 정보를 찾은 뒤, 그 회원 ID 기준으로 일정 조회/등록/삭제를 처리한다.
 * 컨트롤러가 받은 요청 데이터를 실제 저장 가능한 형태로 가공하고 검증한다.
 */
package com.example.membersite.service;

import com.example.membersite.dto.ScheduleCreateRequest;
import com.example.membersite.dto.ScheduleResponse;
import com.example.membersite.entity.Member;
import com.example.membersite.entity.Schedule;
import com.example.membersite.repository.ScheduleRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {

    // 일정 서비스는 로그인 사용자와 일정 저장소를 연결해 비즈니스 규칙을 처리한다.
    private final ScheduleRepository scheduleRepository;
    private final MemberService memberService;

    public ScheduleService(ScheduleRepository scheduleRepository, MemberService memberService) {
        this.scheduleRepository = scheduleRepository;
        this.memberService = memberService;
    }

    // 월 단위 조회를 위해 시작일과 마지막 날짜를 계산한다.
    // 누구의 일정인지 몇 년 몇 월 일정인지를 받아서 그 달 일정 목록을 돌려주는 함수
    // loginID를 이용하여 해당 멤버를 찾음 -> 해당 월의 시작일 만듦 -> 해당 월의 마지막 일 만듦 - > 그 달의 일정을 조회 ->
    public List<ScheduleResponse> findMonthlySchedules(String loginId, int year, int month) {
        Member member = memberService.findByLoginId(loginId);
        LocalDate startDate = LocalDate.of(year, month, 1);  // ex) (2026, 4, 1)
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth()); // ex) 4월에 총일 수 반환 -> 날짜 객체에서 [일]만 바꿈

        List<Schedule> schedules = scheduleRepository
                .findByMemberIdAndPlanDateBetweenOrderByPlanDateAscIdAsc(member.getId(), startDate, endDate);
        List<ScheduleResponse> responses = new ArrayList<>();
        for (Schedule schedule : schedules) {
            responses.add(ScheduleResponse.from(schedule));
        }
        return responses;
    }

    // 반환: 해당 날짜에 속한 일정 응답 목록
    public List<ScheduleResponse> findDailySchedules(String loginId, LocalDate date) {
        Member member = memberService.findByLoginId(loginId);
        List<Schedule> schedules = scheduleRepository.findByMemberIdAndPlanDateOrderByIdAsc(member.getId(), date);
        List<ScheduleResponse> responses = new ArrayList<>();
        for (Schedule schedule : schedules) {
            responses.add(ScheduleResponse.from(schedule));
        }
        return responses;
    }

    // 서버에서도 한 번 더 검증한 뒤 일정 내용을 저장한다.
    // 반환: 저장이 완료된 일정 응답 객체
    public ScheduleResponse create(String loginId, ScheduleCreateRequest request) {
        validate(request);

        Member member = memberService.findByLoginId(loginId);
        Schedule schedule = new Schedule(member.getId(), request.getDate(), request.getContent().trim());
        return ScheduleResponse.from(scheduleRepository.save(schedule));
    }

    // 삭제 전에 본인 일정인지 확인해 다른 사용자의 데이터를 건드리지 않게 한다.
    public void delete(String loginId, Long scheduleId) {
        Member member = memberService.findByLoginId(loginId);
        Schedule schedule = scheduleRepository.findByIdAndMemberId(scheduleId, member.getId());
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule not found.");
        }
        scheduleRepository.deleteById(schedule.getId());
    }

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
