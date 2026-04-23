package com.example.membersite.repository;

import com.example.membersite.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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

    private static final RowMapper<Member> MEMBER_ROW_MAPPER = (resultSet, rowNum) -> new Member(
            resultSet.getLong("id"),
            resultSet.getString("login_id"),
            resultSet.getString("password"),
            resultSet.getString("name")
    );

    private final JdbcTemplate jdbcTemplate;

    /**
     * Inserts a member row.
     *
     * @param member member entity
     */
    public void save(Member member) {
        jdbcTemplate.update(
                INSERT_MEMBER_SQL,
                member.getLoginId(),
                member.getPassword(),
                member.getName()
        );
    }

    /**
     * Checks whether a login id already exists.
     *
     * @param loginId login id
     * @return true when duplicated
     */
    public boolean existsByLoginId(String loginId) {
        Integer memberCount = jdbcTemplate.queryForObject(COUNT_BY_LOGIN_ID_SQL, Integer.class, loginId);
        return memberCount != null && memberCount > 0;
    }

    /**
     * Finds a member by login id.
     *
     * @param loginId login id
     * @return member entity or null when not found
     */
    //SELECT 결과가 정확히 1건일 때, 그 값을 객체 하나로 받아오는 메서드
    public Member findByLoginId(String loginId) {
        try {
            return jdbcTemplate.queryForObject(FIND_BY_LOGIN_ID_SQL, MEMBER_ROW_MAPPER, loginId);
        } catch (EmptyResultDataAccessException exception) {
            return null;
        }
    }

    /**
     * Updates member name by id.
     *
     * @param memberId member id
     * @param name new name
     */
    public void updateName(Long memberId, String name) {
        jdbcTemplate.update(UPDATE_NAME_SQL, name, memberId);
    }

    /**
     * Updates member password hash by id.
     *
     * @param memberId member id
     * @param password password hash
     */
    public void updatePassword(Long memberId, String password) {
        jdbcTemplate.update(UPDATE_PASSWORD_SQL, password, memberId);
    }
}
