package com.example.membersite.support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JdbcConnection {

    private final String url;
    private final String username;
    private final String password;

    public JdbcConnection(@Value("${app.jdbc.url}") String url, @Value("${app.jdbc.username}") String username, @Value("${app.jdbc.password}") String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    // 드라이버를 통해 dbms에 접속
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
