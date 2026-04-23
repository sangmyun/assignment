package com.example.membersite.interceptor;

import com.example.membersite.support.AuthTokenManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class LoginCheckInterceptor implements HandlerInterceptor {

    public static final String LOGIN_ID_ATTRIBUTE = "loginId";

    private final AuthTokenManager authTokenManager;

    /**
     * Validates authentication for protected routes and stores login id in request.
     *
     * @param request servlet request
     * @param response servlet response
     * @param handler handler object
     * @return true to continue, false to stop the request
     * @throws Exception from response handling
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String loginId = authTokenManager.getLoginId(request);
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
