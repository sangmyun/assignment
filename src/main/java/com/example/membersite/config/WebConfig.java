package com.example.membersite.config;

import com.example.membersite.interceptor.LoginCheckInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginCheckInterceptor loginCheckInterceptor;

    /*
    public WebConfig(LoginCheckInterceptor loginCheckInterceptor) {
        this.loginCheckInterceptor = loginCheckInterceptor;
    }
    */

    // 서버를 시작할때 아래 규칙을 등록 -> 스프링이 기억해두고 그 규칙대로 호출 여부 결정
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
                // 아래 주소들은 로그인한 사용자만 접근하게 만든다.
                .addPathPatterns("/dashboard", "/me/**", "/api/schedules/**")
                // 로그인/회원가입/정적 파일은 누구나 접근할 수 있어야 한다.
                .excludePathPatterns("/", "/login", "/signup", "/css/**", "/js/**");
    }
}
