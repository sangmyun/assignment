package com.example.membersite.repository;

import com.example.membersite.entity.Member;
import com.example.membersite.entity.Schedule;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByMemberAndPlanDateBetweenOrderByPlanDateAscIdAsc(Member member, LocalDate startDate, LocalDate endDate);

    List<Schedule> findByMemberAndPlanDateOrderByIdAsc(Member member, LocalDate planDate);

    Optional<Schedule> findByIdAndMember(Long id, Member member);
}
