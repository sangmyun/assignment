package com.example.membersite.support;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenManager {

    private static final String AUTH_COOKIE_NAME = "memberAuthToken";

    private final JwtTokenProvider jwtTokenProvider;
    @Value("${app.auth.jwt.cookie-max-age-seconds:43200}")
    private int cookieMaxAgeSeconds;

    public AuthTokenManager(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public void createToken(String loginId, HttpServletResponse response) {
        String token = jwtTokenProvider.createToken(loginId);
        response.addCookie(createCookie(AUTH_COOKIE_NAME, token, cookieMaxAgeSeconds));
    }

    public String getLoginId(HttpServletRequest request) {
        Cookie cookie = findCookie(request, AUTH_COOKIE_NAME);
        if (cookie == null || cookie.getValue() == null || cookie.getValue().isBlank()) {
            return null;
        }
        return jwtTokenProvider.getLoginId(cookie.getValue());
    }


    public void expire(HttpServletResponse response) {
        response.addCookie(createCookie(AUTH_COOKIE_NAME, "", 0));
    }


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

    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}
