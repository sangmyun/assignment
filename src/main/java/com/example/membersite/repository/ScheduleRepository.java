package com.example.membersite.repository;

import com.example.membersite.entity.Schedule;
import com.example.membersite.support.JdbcConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScheduleRepository {

    private static final String FIND_BY_MEMBER_ID_AND_PLAN_DATE_BETWEEN_SQL = """
            select id, member_id, plan_date, content, created_at
            from schedules
            where member_id = ? and plan_date between ? and ?
            order by plan_date asc, id asc
            """;
    private static final String FIND_BY_MEMBER_ID_AND_PLAN_DATE_SQL = """
            select id, member_id, plan_date, content, created_at
            from schedules
            where member_id = ? and plan_date = ?
            order by id asc
            """;
    private static final String INSERT_SCHEDULE_SQL =
            "insert into schedules (member_id, plan_date, content, created_at) values (?, ?, ?, ?)";
    private static final String FIND_BY_ID_AND_MEMBER_ID_SQL = """
            select id, member_id, plan_date, content, created_at
            from schedules
            where id = ? and member_id = ?
            """;
    private static final String DELETE_BY_ID_SQL =
            "delete from schedules where id = ?";

    private final JdbcConnection connection;

    /*
    public ScheduleRepository(JdbcConnection connection) {
        this.connection = connection;
    }
    */

    public List<Schedule> findByMemberIdAndPlanDateBetweenOrderByPlanDateAscIdAsc(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate) {
        return querySchedules(
                FIND_BY_MEMBER_ID_AND_PLAN_DATE_BETWEEN_SQL,
                statement -> {
                    statement.setLong(1, memberId);
                    statement.setObject(2, startDate);
                    statement.setObject(3, endDate);
                }
        );
    }

    public List<Schedule> findByMemberIdAndPlanDateOrderByIdAsc(Long memberId, LocalDate planDate) {
        return querySchedules(
                FIND_BY_MEMBER_ID_AND_PLAN_DATE_SQL,
                statement -> {
                    statement.setLong(1, memberId);
                    statement.setObject(2, planDate);
                }
        );
    }

    public Schedule save(Schedule schedule) {
        try (Connection dbConnection = connection.getConnection();
             PreparedStatement statement = createInsertStatement(dbConnection)) {
            bindScheduleValues(statement, schedule);
            statement.executeUpdate();

            Long createdScheduleId = extractGeneratedScheduleId(statement);
            return new Schedule(
                    createdScheduleId,
                    schedule.getMemberId(),
                    schedule.getPlanDate(),
                    schedule.getContent(),
                    schedule.getCreatedAt()
            );
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save schedule.", exception);
        }
    }

    public Schedule findByIdAndMemberId(Long scheduleId, Long memberId) {
        try (Connection dbConnection = connection.getConnection();
             PreparedStatement statement = dbConnection.prepareStatement(FIND_BY_ID_AND_MEMBER_ID_SQL)) {
            statement.setLong(1, scheduleId);
            statement.setLong(2, memberId);
            return fetchSingleSchedule(statement);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to find schedule.", exception);
        }
    }

    public void deleteById(Long scheduleId) {
        executeUpdate(DELETE_BY_ID_SQL, statement -> statement.setLong(1, scheduleId));
    }

    private List<Schedule> querySchedules(String sql, StatementSetter statementSetter) {
        try (Connection dbConnection = connection.getConnection();
             PreparedStatement statement = dbConnection.prepareStatement(sql)) {
            statementSetter.setValues(statement);
            return fetchSchedules(statement);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to query schedules.", exception);
        }
    }

    private void executeUpdate(String sql, StatementSetter statementSetter) {
        try (Connection dbConnection = connection.getConnection();
             PreparedStatement statement = dbConnection.prepareStatement(sql)) {
            statementSetter.setValues(statement);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to update schedule.", exception);
        }
    }

    private void bindScheduleValues(PreparedStatement statement, Schedule schedule) throws SQLException {
        statement.setLong(1, schedule.getMemberId());
        statement.setObject(2, schedule.getPlanDate());
        statement.setString(3, schedule.getContent());
        statement.setObject(4, schedule.getCreatedAt());
    }

    private PreparedStatement createInsertStatement(Connection dbConnection) throws SQLException {
        return dbConnection.prepareStatement(INSERT_SCHEDULE_SQL, PreparedStatement.RETURN_GENERATED_KEYS);
    }

    private Long extractGeneratedScheduleId(PreparedStatement statement) throws SQLException {
        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            if (!generatedKeys.next()) {
                throw new IllegalStateException("Failed to create schedule.");
            }
            return generatedKeys.getLong(1);
        }
    }

    private Schedule fetchSingleSchedule(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                return null;
            }

            return mapSchedule(resultSet);
        }
    }

    private List<Schedule> fetchSchedules(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            List<Schedule> schedules = new ArrayList<>();
            while (resultSet.next()) {
                schedules.add(mapSchedule(resultSet));
            }
            return schedules;
        }
    }

    private Schedule mapSchedule(ResultSet resultSet) throws SQLException {
        return new Schedule(
                resultSet.getLong("id"),
                resultSet.getLong("member_id"),
                resultSet.getDate("plan_date").toLocalDate(),
                resultSet.getString("content"),
                resultSet.getTimestamp("created_at").toLocalDateTime()
        );
    }

    @FunctionalInterface
    private interface StatementSetter {
        void setValues(PreparedStatement statement) throws SQLException;
    }
}
