/*
 * members 테이블과 직접 통신하는 저장소.
 * 회원 저장, loginId 중복 확인, 회원 조회, 이름/비밀번호 수정을 SQL로 처리한다.
 * Service는 이 클래스를 통해 DB에서 Member 데이터를 읽고 쓴다.
 */
package com.example.membersite.repository;

import com.example.membersite.entity.Member;
<<<<<<< HEAD
import com.example.membersite.support.JdbcConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
=======
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
>>>>>>> 6926320 (nointercepter)
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepository {

<<<<<<< HEAD
    private static final String INSERT_MEMBER_SQL =
            "insert into members (login_id, password, name) values (?, ?, ?)";
    private static final String COUNT_BY_LOGIN_ID_SQL =
            "select count(*) from members where login_id = ?";
    private static final String FIND_BY_LOGIN_ID_SQL =
            "select id, login_id, password, name from members where login_id = ?";
    private static final String UPDATE_NAME_SQL =
            "update members set name = ? where id = ?";
    private static final String UPDATE_PASSWORD_SQL =
            "update members set password = ? where id = ?";

    private final JdbcConnection connection;

    public MemberRepository(JdbcConnection connection) {
        this.connection = connection;
    }

    /*
     * 회원 한 명을 DB에 저장하는 함수.
     * insert SQL을 실행한 뒤, DB가 만든 id까지 포함해서 Member 객체를 반환한다.
     */
    // 반환: DB에 저장된 뒤 생성된 id가 반영된 회원 객체
    public Member save(Member member) {
        Connection dbConnection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;

        try {
            dbConnection = connection.getConnection();
            statement = dbConnection.prepareStatement(
                    INSERT_MEMBER_SQL,
                    PreparedStatement.RETURN_GENERATED_KEYS
            );

            bindMemberValues(statement, member);
            statement.executeUpdate();

            generatedKeys = statement.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new IllegalStateException("Failed to create member.");
            }

            Long createdMemberId = generatedKeys.getLong(1);
            return new Member(
                    createdMemberId,
                    member.getLoginId(),
                    member.getPassword(),
                    member.getName()
            );
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save member.", exception);
        } finally {
            closeResultSet(generatedKeys);
            closeStatement(statement);
            closeConnection(dbConnection);
        }
    }

    /*
     * 입력한 loginId가 이미 DB에 존재하는지 확인하는 함수.
     * 회원 수를 세는 SQL을 실행해서 1명 이상이면 true, 없으면 false를 반환한다.
     */
    // 반환: 동일한 loginId가 존재하면 true, 아니면 false
    public boolean existsByLoginId(String loginId) {
        Connection dbConnection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            dbConnection = connection.getConnection();
            statement = dbConnection.prepareStatement(COUNT_BY_LOGIN_ID_SQL);
            statement.setString(1, loginId);

            resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return false;
            }

            int memberCount = resultSet.getInt(1);
            return memberCount > 0;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to check member loginId.", exception);
        } finally {
            closeResultSet(resultSet);
            closeStatement(statement);
            closeConnection(dbConnection);
        }
    }

    /*
     * loginId로 회원 한 명을 조회하는 함수.
     * 조회 결과가 있으면 Member 객체를 반환하고, 없으면 null을 반환한다.
     */
    // 반환: 조회된 회원 객체, 없으면 null
    public Member findByLoginId(String loginId) {
        Connection dbConnection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            dbConnection = connection.getConnection();
            statement = dbConnection.prepareStatement(FIND_BY_LOGIN_ID_SQL);
            statement.setString(1, loginId);

            resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return null;
            }

            return mapMember(resultSet);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to find member by loginId.", exception);
        } finally {
            closeResultSet(resultSet);
            closeStatement(statement);
            closeConnection(dbConnection);
        }
    }

    /*
     * 회원 id를 기준으로 이름을 수정하는 함수.
     */
    public void updateName(Long memberId, String name) {
        executeUpdate(UPDATE_NAME_SQL, statement -> {
            statement.setString(1, name);
            statement.setLong(2, memberId);
        });
    }

    /*
     * 회원 id를 기준으로 비밀번호를 수정하는 함수.
     */
    public void updatePassword(Long memberId, String password) {
        executeUpdate(UPDATE_PASSWORD_SQL, statement -> {
            statement.setString(1, password);
            statement.setLong(2, memberId);
        });
    }

    /*
     * update SQL을 공통으로 실행하는 함수.
     * 이름 수정, 비밀번호 수정처럼 반환값이 없는 변경 작업에 사용한다.
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
            throw new IllegalStateException("Failed to update member.", exception);
        } finally {
            closeStatement(statement);
            closeConnection(dbConnection);
        }
    }

    /*
     * insert SQL의 ? 자리에 회원 정보를 넣는 함수.
     */
    private void bindMemberValues(PreparedStatement statement, Member member) throws SQLException {
        statement.setString(1, member.getLoginId());
        statement.setString(2, member.getPassword());
        statement.setString(3, member.getName());
    }

    /*
     * ResultSet의 현재 행 데이터를 Member 객체로 바꾸는 함수.
     */
    // 반환: 현재 ResultSet 행을 옮겨 담은 Member 객체
    private Member mapMember(ResultSet resultSet) throws SQLException {
        return new Member(
                resultSet.getLong("id"),
                resultSet.getString("login_id"),
                resultSet.getString("password"),
                resultSet.getString("name")
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
=======
    private final JdbcTemplate jdbcTemplate;

    public MemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Member save(Member member) {
        jdbcTemplate.update(
                "insert into members (login_id, password, name) values (?, ?, ?)",
                member.getLoginId(),
                member.getPassword(),
                member.getName()
        );
        return findByLoginId(member.getLoginId()).orElseThrow();
    }

    public boolean existsByLoginId(String loginId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from members where login_id = ?",
                Integer.class,
                loginId
        );
        return count != null && count > 0;
    }

    public Optional<Member> findByLoginId(String loginId) {
        List<Member> members = jdbcTemplate.query(
                "select id, login_id, password, name from members where login_id = ?",
                this::mapRow,
                loginId
        );
        return members.stream().findFirst();
    }

    public Optional<Member> findById(Long id) {
        List<Member> members = jdbcTemplate.query(
                "select id, login_id, password, name from members where id = ?",
                this::mapRow,
                id
        );
        return members.stream().findFirst();
    }

    public void updateName(String loginId, String name) {
        jdbcTemplate.update(
                "update members set name = ? where login_id = ?",
                name,
                loginId
        );
    }

    public void updatePassword(String loginId, String password) {
        jdbcTemplate.update(
                "update members set password = ? where login_id = ?",
                password,
                loginId
        );
    }

    private Member mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Member(
                rs.getLong("id"),
                rs.getString("login_id"),
                rs.getString("password"),
                rs.getString("name"),
                null,
                null
        );
>>>>>>> 6926320 (nointercepter)
    }
}
