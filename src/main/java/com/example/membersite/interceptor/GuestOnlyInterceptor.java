package com.example.membersite.interceptor;

import com.example.membersite.support.AuthTokenManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class GuestOnlyInterceptor implements HandlerInterceptor {

    private final AuthTokenManager authTokenManager;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        if (authTokenManager.getLoginId(request) != null) {
            response.sendRedirect("/dashboard");
            return false;
        }

        return true;
    }
}
