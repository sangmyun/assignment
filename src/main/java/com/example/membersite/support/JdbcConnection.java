package com.example.membersite.support;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.stereotype.Component;

@Component
public class JdbcConnection {

    private final DataSource dataSource;

    public JdbcConnection(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Uses the same DataSource that Spring SQL initialization uses.
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
