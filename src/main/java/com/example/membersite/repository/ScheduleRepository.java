/*
 * schedules 테이블과 직접 통신하는 저장소.
 * 회원 ID와 날짜를 기준으로 일정 목록을 조회하고, 일정 저장과 삭제를 SQL로 처리한다.
 * Service는 이 클래스를 통해 DB에서 Schedule 데이터를 읽고 쓴다.
 */
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
import org.springframework.stereotype.Repository;

@Repository
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

    public ScheduleRepository(JdbcConnection connection) {
        this.connection = connection;
    }

    /*
     * 회원 ID와 시작일/종료일을 기준으로 월간 일정 목록을 조회하는 함수.
     * 해당 기간에 포함되는 일정을 날짜순, id순으로 정렬해서 반환한다.
     */
    // 반환: 기간 조건에 맞는 일정 목록
    public List<Schedule> findByMemberIdAndPlanDateBetweenOrderByPlanDateAscIdAsc(  // 객체를 담는 리스트..
            Long memberId,
            LocalDate startDate,
            LocalDate endDate)
    {
        return querySchedules(
                FIND_BY_MEMBER_ID_AND_PLAN_DATE_BETWEEN_SQL,
                statement -> {
                    statement.setLong(1, memberId);
                    statement.setObject(2, startDate);
                    statement.setObject(3, endDate);
                }
        );
    }

    /*
     * 회원 ID와 특정 날짜를 기준으로 하루 일정 목록을 조회하는 함수.
     * 같은 날짜의 일정을 id 순서대로 반환한다.
     */
    // 반환: 특정 날짜 조건에 맞는 일정 목록
    public List<Schedule> findByMemberIdAndPlanDateOrderByIdAsc(Long memberId, LocalDate planDate) {
        return querySchedules(
                FIND_BY_MEMBER_ID_AND_PLAN_DATE_SQL,
                statement -> {
                    statement.setLong(1, memberId);
                    statement.setObject(2, planDate);
                }
        );
    }

    /*
     * 일정 한 건을 DB에 저장하는 함수.
     * insert SQL을 실행한 뒤, DB가 만든 id까지 포함해서 Schedule 객체를 반환한다.
     */
    // 반환: DB에 저장된 뒤 생성된 id가 반영된 일정 객체
    public Schedule save(Schedule schedule) {
        Connection dbConnection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;

        try {
            dbConnection = connection.getConnection();
            statement = dbConnection.prepareStatement(
                    INSERT_SCHEDULE_SQL,
                    PreparedStatement.RETURN_GENERATED_KEYS
            );

            bindScheduleValues(statement, schedule);
            statement.executeUpdate();

            generatedKeys = statement.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new IllegalStateException("Failed to create schedule.");
            }

            Long createdScheduleId = generatedKeys.getLong(1);
            return new Schedule(
                    createdScheduleId,
                    schedule.getMemberId(),
                    schedule.getPlanDate(),
                    schedule.getContent(),
                    schedule.getCreatedAt()
            );
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save schedule.", exception);
        } finally {
            closeResultSet(generatedKeys);
            closeStatement(statement);
            closeConnection(dbConnection);
        }
    }

    /*
     * 일정 ID와 회원 ID가 모두 일치하는 일정 한 건을 조회하는 함수.
     * 조회 결과가 있으면 Schedule 객체를 반환하고, 없으면 null을 반환한다.
     */
    // 반환: 일정 ID와 회원 ID가 모두 일치하는 일정 객체, 없으면 null
    public Schedule findByIdAndMemberId(Long scheduleId, Long memberId) {
        Connection dbConnection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            dbConnection = connection.getConnection();
            statement = dbConnection.prepareStatement(FIND_BY_ID_AND_MEMBER_ID_SQL);
            statement.setLong(1, scheduleId);
            statement.setLong(2, memberId);

            resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return null;
            }

            return mapSchedule(resultSet);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to find schedule.", exception);
        } finally {
            closeResultSet(resultSet);
            closeStatement(statement);
            closeConnection(dbConnection);
        }
    }

    /*
     * 일정 ID를 기준으로 일정 한 건을 삭제하는 함수.
     */
    public void deleteById(Long scheduleId) {
        executeUpdate(DELETE_BY_ID_SQL, statement -> statement.setLong(1, scheduleId));
    }

    /*
     * 일정 조회 SQL을 공통으로 실행하는 함수.
     * 조회 결과를 Schedule 리스트로 만들어 반환한다.
     */
    // 연결
    // 반환: 조회 SQL 실행 결과를 옮겨 담은 일정 목록
    private List<Schedule> querySchedules(String sql, StatementSetter statementSetter) {
        Connection dbConnection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            dbConnection = connection.getConnection();
            statement = dbConnection.prepareStatement(sql);
            statementSetter.setValues(statement);

            resultSet = statement.executeQuery();
            List<Schedule> schedules = new ArrayList<>();
            while (resultSet.next()) {
                schedules.add(mapSchedule(resultSet));
            }

            return schedules;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to query schedules.", exception);
        } finally {
            closeResultSet(resultSet);
            closeStatement(statement);
            closeConnection(dbConnection);
        }
    }

    /*
     * update 또는 delete SQL을 공통으로 실행하는 함수.
     */
    private void executeUpdate(String sql, StatementSetter statementSetter) {
        Connection dbConnection = null;
        PreparedStatement statement = null;

        try {
            dbConnection = connection.getConnection();
            statement = dbConnection.prepareStatement(sql);
            statementSetter.setValues(statement);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to update schedule.", exception);
        } finally {
            closeStatement(statement);
            closeConnection(dbConnection);
        }
    }

    /*
     * insert SQL의 ? 자리에 일정 정보를 넣는 함수.
     */
    private void bindScheduleValues(PreparedStatement statement, Schedule schedule) throws SQLException {
        statement.setLong(1, schedule.getMemberId());
        statement.setObject(2, schedule.getPlanDate());
        statement.setString(3, schedule.getContent());
        statement.setObject(4, schedule.getCreatedAt());
    }

    /*
     * ResultSet의 현재 행 데이터를 Schedule 객체로 바꾸는 함수.
     */
    // 반환: 현재 ResultSet 행을 옮겨 담은 Schedule 객체
    private Schedule mapSchedule(ResultSet resultSet) throws SQLException {
        return new Schedule(
                resultSet.getLong("id"),
                resultSet.getLong("member_id"),
                resultSet.getDate("plan_date").toLocalDate(),
                resultSet.getString("content"),
                resultSet.getTimestamp("created_at").toLocalDateTime()
        );
    }

    /*
     * ResultSet이 null이 아니면 닫는 함수.
     */
    private void closeResultSet(ResultSet resultSet) {
        if (resultSet == null) {
            return;
        }

        try {
            resultSet.close();
        } catch (SQLException ignored) {
        }
    }

    /*
     * PreparedStatement가 null이 아니면 닫는 함수.
     */
    private void closeStatement(PreparedStatement statement) {
        if (statement == null) {
            return;
        }

        try {
            statement.close();
        } catch (SQLException ignored) {
        }
    }

    /*
     * DB Connection이 null이 아니면 닫는 함수.
     */
    private void closeConnection(Connection dbConnection) {
        if (dbConnection == null) {
            return;
        }

        try {
            dbConnection.close();
        } catch (SQLException ignored) {
        }
    }

    @FunctionalInterface
    private interface StatementSetter {
        void setValues(PreparedStatement statement) throws SQLException;
    }
}
