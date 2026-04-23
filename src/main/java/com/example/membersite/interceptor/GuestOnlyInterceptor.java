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

    /**
     * Blocks guest-only pages for already authenticated users.
     *
     * @param request servlet request
     * @param response servlet response
     * @param handler handler object
     * @return true to continue, false to stop after redirect
     * @throws Exception from response handling
     */
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
