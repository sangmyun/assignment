package com.example.membersite.support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JdbcConnection {

    // application.yml의 JDBC 설정을 주입받아 매번 새 Connection을 연다.
    private final String url;
    private final String username;
    private final String password;

    public JdbcConnection(@Value("${app.jdbc.url}") String url,
                          @Value("${app.jdbc.username}") String username,
                          @Value("${app.jdbc.password}") String password,
                          @Value("${app.jdbc.driver-class-name}") String driverClassName) {
        this.url = url;
        this.username = username;
        this.password = password;

        try {
            // DriverManager가 어떤 드라이버로 연결할지 알 수 있도록 미리 로딩한다.
            Class.forName(driverClassName);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Failed to load JDBC driver.", exception);
        }
    }

    // JDBC 작업은 Connection 확보부터 시작한다.
    // 반환: JDBC 작업에 사용할 새 DB 연결 객체
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
