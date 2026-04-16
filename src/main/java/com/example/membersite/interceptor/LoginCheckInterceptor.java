package com.example.membersite.interceptor;

import com.example.membersite.support.SessionManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class LoginCheckInterceptor implements HandlerInterceptor {

    // Interceptor puts loginId here so controllers can reuse it.
    public static final String LOGIN_ID_ATTRIBUTE = "loginId";

    private final SessionManager sessionManager;

    /*
    public LoginCheckInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
    */





    // true: DispatcherServlet이 컨트롤러로 진행
    // false: DispatcherServlet이 처리 중단 (컨트롤러 미호출)
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String loginId = sessionManager.getLoginId(request);
        if (loginId != null) {
            request.setAttribute(LOGIN_ID_ATTRIBUTE, loginId);
            return true;
        }


        if (request.getRequestURI().startsWith("/api/")) {
            response.sendError(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        response.sendRedirect("/login");
        return false;
    }
}
