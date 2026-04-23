package com.example.membersite.support;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class temp_SessionManager {

    private static final String SESSION_COOKIE_NAME = "memberSessionId";
    private final Map<String, String> sessionStore = new ConcurrentHashMap<>();

    /**
     * Creates a server-side session mapping and writes session id cookie.
     *
     * @param loginId login id
     * @param response servlet response
     */
    public void createSession(String loginId, HttpServletResponse response) {
        String sessionId = UUID.randomUUID().toString();
        sessionStore.put(sessionId, loginId);
        Cookie cookie = createCookie(SESSION_COOKIE_NAME, sessionId, -1);
        response.addCookie(cookie);
    }

    /**
     * Resolves login id from session cookie and in-memory session store.
     *
     * @param request servlet request
     * @return login id or null
     */
    public String getLoginId(HttpServletRequest request) {
        Cookie cookie = findCookie(request, SESSION_COOKIE_NAME);
        if (cookie == null) {
            return null;
        }
        return sessionStore.get(cookie.getValue());
    }

    /**
     * Expires session mapping and cookie.
     *
     * @param request servlet request
     * @param response servlet response
     */
    public void expire(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = findCookie(request, SESSION_COOKIE_NAME);
        if (cookie == null) {
            return;
        }

        sessionStore.remove(cookie.getValue());
        response.addCookie(createCookie(SESSION_COOKIE_NAME, "", 0));
    }

    /**
     * Finds a cookie by name from request.
     *
     * @param request servlet request
     * @param cookieName cookie name
     * @return cookie or null
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
     * Creates a HttpOnly cookie.
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
