package com.example.membersite.config;

import com.example.membersite.interceptor.GuestOnlyInterceptor;
import com.example.membersite.interceptor.LoginCheckInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final GuestOnlyInterceptor guestOnlyInterceptor;
    private final LoginCheckInterceptor loginCheckInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(guestOnlyInterceptor)
                .addPathPatterns("/", "/login", "/signup");

        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns("/dashboard", "/me/**", "/api/schedules/**", "/boards/**")
                .excludePathPatterns("/", "/login", "/signup", "/css/**", "/js/**");
    }
}
