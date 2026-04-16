package com.example.membersite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MemberSiteApplication {

    // 스프링 부트가 내장 서버를 띄우고, 필요한 빈을 자동으로 등록한다.
    public static void main(String[] args) {
        SpringApplication.run(MemberSiteApplication.class, args);
    }
}
