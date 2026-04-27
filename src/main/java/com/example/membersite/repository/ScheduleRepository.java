package com.example.membersite.repository;

import com.example.membersite.entity.Schedule;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScheduleRepository {

    private final ScheduleMapper scheduleMapper;

    /**
     * Finds schedules between dates for a member.
     *
     * @param memberId member id
     * @param startDate start date
     * @param endDate end date
     * @return ordered schedules
     */
    public List<Schedule> findByMemberIdAndPlanDateBetweenOrderByPlanDateAscDisplayOrderAscIdAsc(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate) {
        return scheduleMapper.findByMemberIdAndPlanDateBetweenOrderByPlanDateAscDisplayOrderAscIdAsc(
                memberId,
                startDate,
                endDate
        );
    }

    /**
     * Finds schedules of a specific date for a member.
     *
     * @param memberId member id
     * @param planDate plan date
     * @return ordered schedules
     */
    public List<Schedule> findByMemberIdAndPlanDateOrderByDisplayOrderAscIdAsc(Long memberId, LocalDate planDate) {
        return scheduleMapper.findByMemberIdAndPlanDateOrderByDisplayOrderAscIdAsc(memberId, planDate);
    }

    /**
     * Inserts a schedule row and returns the saved entity with generated id.
     *
     * @param schedule schedule entity
     * @return saved schedule with id
     */
    public Schedule save(Schedule schedule) {
        ScheduleMapper.ScheduleInsertParam param = new ScheduleMapper.ScheduleInsertParam(
                schedule.getMemberId(),
                schedule.getPlanDate(),
                schedule.getContent(),
                schedule.getDisplayOrder(),
                schedule.getCreatedAt()
        );
        int affectedRows = scheduleMapper.insert(param);
        if (affectedRows != 1 || param.getId() == null) {
            throw new IllegalStateException("Failed to create schedule.");
        }

        return new Schedule(
                param.getId(),
                schedule.getMemberId(),
                schedule.getPlanDate(),
                schedule.getContent(),
                schedule.getDisplayOrder(),
                schedule.getCreatedAt()
        );
    }

    /**
     * Finds max display order for a member/date.
     *
     * @param memberId member id
     * @param planDate plan date
     * @return max display order, zero when empty
     */
    public int findMaxDisplayOrderByMemberIdAndPlanDate(Long memberId, LocalDate planDate) {
        Integer maxOrder = scheduleMapper.findMaxDisplayOrderByMemberIdAndPlanDate(memberId, planDate);
        return maxOrder == null ? 0 : maxOrder;
    }

    /**
     * Updates display order by schedule id and owner member id.
     *
     * @param scheduleId schedule id
     * @param memberId member id
     * @param displayOrder new order
     * @return affected row count
     */
    public int updateDisplayOrderByIdAndMemberId(Long scheduleId, Long memberId, int displayOrder) {
        return scheduleMapper.updateDisplayOrderByIdAndMemberId(scheduleId, memberId, displayOrder);
    }

    /**
     * Finds a schedule by schedule id and owner member id.
     *
     * @param scheduleId schedule id
     * @param memberId member id
     * @return schedule entity or null when not found
     */
    public Schedule findByIdAndMemberId(Long scheduleId, Long memberId) {
        return scheduleMapper.findByIdAndMemberId(scheduleId, memberId);
    }

    /**
     * Deletes a schedule row by id.
     *
     * @param scheduleId schedule id
     */
    public void deleteById(Long scheduleId) {
        scheduleMapper.deleteById(scheduleId);
    }
}
