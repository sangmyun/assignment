package com.example.membersite.repository;

import com.example.membersite.entity.Post;
import com.example.membersite.support.JdbcConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PostRepository {

    private static final String FIND_ALL_SQL = """
            select p.id, p.member_id, m.name as author_name, p.title, p.content, p.created_at, p.updated_at
            from posts p
            join members m on m.id = p.member_id
            order by p.id desc
            """;

    private static final String FIND_BY_ID_SQL = """
            select p.id, p.member_id, m.name as author_name, p.title, p.content, p.created_at, p.updated_at
            from posts p
            join members m on m.id = p.member_id
            where p.id = ?
            """;

    private static final String FIND_BY_ID_AND_MEMBER_ID_SQL = """
            select p.id, p.member_id, m.name as author_name, p.title, p.content, p.created_at, p.updated_at
            from posts p
            join members m on m.id = p.member_id
            where p.id = ? and p.member_id = ?
            """;

    private static final String INSERT_SQL =
            "insert into posts (member_id, title, content, created_at, updated_at) values (?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL =
            "update posts set title = ?, content = ?, updated_at = ? where id = ?";
    private static final String DELETE_SQL =
            "delete from posts where id = ?";

    private final JdbcConnection connection;

    public PostRepository(JdbcConnection connection) {
        this.connection = connection;
    }

    public List<Post> findAllOrderByIdDesc() {
        try (Connection dbConnection = connection.getConnection();
             PreparedStatement statement = dbConnection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            List<Post> posts = new ArrayList<>();
            while (resultSet.next()) {
                posts.add(mapPost(resultSet));
            }
            return posts;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to find posts.", exception);
        }
    }

    public Post findById(Long postId) {
        return queryOne(FIND_BY_ID_SQL, statement -> statement.setLong(1, postId));
    }

    public Post findByIdAndMemberId(Long postId, Long memberId) {
        return queryOne(FIND_BY_ID_AND_MEMBER_ID_SQL, statement -> {
            statement.setLong(1, postId);
            statement.setLong(2, memberId);
        });
    }

    public Post save(Post post) {
        try (Connection dbConnection = connection.getConnection();
             PreparedStatement statement = dbConnection.prepareStatement(
                     INSERT_SQL,
                     PreparedStatement.RETURN_GENERATED_KEYS
             )) {
            statement.setLong(1, post.getMemberId());
            statement.setString(2, post.getTitle());
            statement.setString(3, post.getContent());
            statement.setObject(4, post.getCreatedAt());
            statement.setObject(5, post.getUpdatedAt());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new IllegalStateException("Failed to create post.");
                }
                Long postId = generatedKeys.getLong(1);
                return findById(postId);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save post.", exception);
        }
    }

    public void update(Long postId, String title, String content, LocalDateTime updatedAt) {
        executeUpdate(UPDATE_SQL, statement -> {
            statement.setString(1, title);
            statement.setString(2, content);
            statement.setObject(3, updatedAt);
            statement.setLong(4, postId);
        });
    }

    public void deleteById(Long postId) {
        executeUpdate(DELETE_SQL, statement -> statement.setLong(1, postId));
    }

    private Post queryOne(String sql, StatementSetter setter) {
        try (Connection dbConnection = connection.getConnection();
             PreparedStatement statement = dbConnection.prepareStatement(sql)) {
            setter.setValues(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapPost(resultSet);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to query post.", exception);
        }
    }

    private void executeUpdate(String sql, StatementSetter setter) {
        try (Connection dbConnection = connection.getConnection();
             PreparedStatement statement = dbConnection.prepareStatement(sql)) {
            setter.setValues(statement);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to update post.", exception);
        }
    }

    private Post mapPost(ResultSet resultSet) throws SQLException {
        return new Post(
                resultSet.getLong("id"),
                resultSet.getLong("member_id"),
                resultSet.getString("author_name"),
                resultSet.getString("title"),
                resultSet.getString("content"),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getTimestamp("updated_at").toLocalDateTime()
        );
    }

    @FunctionalInterface
    private interface StatementSetter {
        void setValues(PreparedStatement statement) throws SQLException;
    }
}
