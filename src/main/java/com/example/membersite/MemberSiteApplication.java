package com.example.membersite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MemberSiteApplication {

    // 스프링 부트가 내장 서버를 띄우고, 필요한 빈을 자동으로 등록한다.
    public static void main(String[] args) {
        // 스프링 부트가 서버 실행, 객체 생성, 설정 읽기를 한 번에 시작한다.
        SpringApplication.run(MemberSiteApplication.class, args);
    }
}
