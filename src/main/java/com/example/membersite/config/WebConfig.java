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

    // 적용 범위를 정의, 등록된 경로에 요청이 들어오면 인터셉터 실행
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
                // 아래 주소들은 로그인한 사용자만 접근하게 만든다.
                .addPathPatterns("/dashboard", "/me/**", "/api/schedules/**")
                // 로그인/회원가입/정적 파일은 누구나 접근할 수 있어야 한다.
                .excludePathPatterns("/", "/login", "/signup", "/css/**", "/js/**");
    }
}
