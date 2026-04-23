package com.example.membersite.support;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthTokenManager {

    private static final String AUTH_COOKIE_NAME = "memberAuthToken";

    private final JwtTokenProvider jwtTokenProvider;
    @Value("${app.auth.jwt.cookie-max-age-seconds:43200}")
    private int cookieMaxAgeSeconds;

    /**
     * Creates JWT token and writes it to cookie.
     *
     * @param loginId login id
     * @param response servlet response
     */
    public void createToken(String loginId, HttpServletResponse response) {
        String token = jwtTokenProvider.createToken(loginId);
        response.addCookie(createCookie(AUTH_COOKIE_NAME, token, cookieMaxAgeSeconds));
    }

    /**
     * Resolves login id from auth cookie and JWT payload.
     *
     * @param request servlet request
     * @return login id or null when token is missing/invalid
     */
    public String getLoginId(HttpServletRequest request) {
        Cookie cookie = findCookie(request, AUTH_COOKIE_NAME);
        if (cookie == null || cookie.getValue() == null || cookie.getValue().isBlank()) {
            return null;
        }
        return jwtTokenProvider.getLoginId(cookie.getValue());
    }

    /**
     * Expires auth cookie immediately.
     *
     * @param response servlet response
     */
    public void expire(HttpServletResponse response) {
        response.addCookie(createCookie(AUTH_COOKIE_NAME, "", 0));
    }

    /**
     * Finds a cookie by name from request.
     *
     * @param request servlet request
     * @param cookieName cookie name
     * @return matching cookie or null
     */
    private Cookie findCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }

    /**
     * Creates a HttpOnly cookie for auth token.
     *
     * @param name cookie name
     * @param value cookie value
     * @param maxAge max age in seconds
     * @return configured cookie
     */
    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}
