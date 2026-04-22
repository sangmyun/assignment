package com.example.membersite.repository;

import com.example.membersite.entity.Member;
import com.example.membersite.support.JdbcConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

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

    /*
    public MemberRepository(JdbcConnection connection) {
        this.connection = connection;
    }
    */

    public Member save(Member member) {
        try (Connection dbConnection = connection.getConnection();
             PreparedStatement statement = dbConnection.prepareStatement(
                     INSERT_MEMBER_SQL,
                     PreparedStatement.RETURN_GENERATED_KEYS // 생성된 키 반환
             )) {
            statement.setString(1, member.getLoginId());
            statement.setString(2, member.getPassword());
            statement.setString(3, member.getName());
            statement.executeUpdate();


            Long createdMemberId;
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new IllegalStateException("Failed to create member.");
                }
                createdMemberId = generatedKeys.getLong(1);
            }
            return new Member(
                    createdMemberId,
                    member.getLoginId(),
                    member.getPassword(),
                    member.getName()
            );
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save member.", exception);
        }
    }

    public boolean existsByLoginId(String loginId) {
        try (Connection dbConnection = connection.getConnection();
             PreparedStatement statement = dbConnection.prepareStatement(COUNT_BY_LOGIN_ID_SQL)) {
            statement.setString(1, loginId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }

                int memberCount = resultSet.getInt(1);
                return memberCount > 0;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to check member loginId.", exception);
        }
    }

    public Member findByLoginId(String loginId) {
        try (Connection dbConnection = connection.getConnection();
             PreparedStatement statement = dbConnection.prepareStatement(FIND_BY_LOGIN_ID_SQL)) {
            statement.setString(1, loginId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapMember(resultSet);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to find member by loginId.", exception);
        }
    }

    public void updateName(Long memberId, String name) {
        executeUpdate(UPDATE_NAME_SQL, statement -> {
            statement.setString(1, name);
            statement.setLong(2, memberId);
        });
    }

    public void updatePassword(Long memberId, String password) {
        executeUpdate(UPDATE_PASSWORD_SQL, statement -> {
            statement.setString(1, password);
            statement.setLong(2, memberId);
        });
    }

    private void executeUpdate(String sql, StatementSetter statementSetter) {
        try (Connection dbConnection = connection.getConnection();
             PreparedStatement statement = dbConnection.prepareStatement(sql)) {
            statementSetter.setValues(statement);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to update member.", exception);
        }
    }
    private Member mapMember(ResultSet resultSet) throws SQLException {
        return new Member(
                resultSet.getLong("id"),
                resultSet.getString("login_id"),
                resultSet.getString("password"),
                resultSet.getString("name")
        );
    }

    @FunctionalInterface
    private interface StatementSetter {
        void setValues(PreparedStatement statement) throws SQLException;
    }
}
