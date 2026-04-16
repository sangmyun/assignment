/*
 * 세션 처리 전용 클래스.
 * 로그인 성공 시 세션 ID를 만들고, 서버 메모리(Map)에 세션 ID와 로그인 ID를 저장한다.
 * 브라우저에는 세션 ID만 쿠키로 내려보낸다.
 * 이후 요청에서는 쿠키를 읽어서 현재 로그인한 사용자의 loginId를 찾는다.
 * 로그아웃할 때는 서버 Map과 브라우저 쿠키를 함께 삭제한다.
 */
package com.example.membersite.support;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class SessionManager {

    // 브라우저에는 세션 id만 저장하고, 로그인 정보는 서버 메모리에 둔다.
    private static final String SESSION_COOKIE_NAME = "memberSessionId";

    // 문자열 키, 문자열 값을 저장하는 Map을 만들고, 변수는 sessionstore ,
    private final Map<String, String> sessionStore = new ConcurrentHashMap<>();

    public void createSession(String loginId, HttpServletResponse response) {
        String sessionId = UUID.randomUUID().toString();
        sessionStore.put(sessionId, loginId);
        Cookie cookie = createCookie(SESSION_COOKIE_NAME, sessionId, -1); // -1은 브라우저가 종료될때까지 유효하다는 의미
        response.addCookie(cookie);
    }

    // 요청 쿠키에서 세션 id를 꺼내 로그인한 사용자의 loginId를 찾는다.
    // 반환: 세션에 연결된 loginId, 세션이 없으면 null
    public String getLoginId(HttpServletRequest request) {
        Cookie cookie = findCookie(request, SESSION_COOKIE_NAME); //cookie 객체 반환
        if (cookie == null) {
            return null;
        }
        return sessionStore.get(cookie.getValue());
    }

    // 로그아웃 시 서버 저장소와 브라우저 쿠키를 모두 지운다.
    public void expire(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = findCookie(request, SESSION_COOKIE_NAME);
        if (cookie == null) {
            return;
        }

        sessionStore.remove(cookie.getValue());
        response.addCookie(createCookie(SESSION_COOKIE_NAME, "", 0));
    }

    // 반환: 이름이 일치하는 쿠키 객체, 없으면 null
    private Cookie findCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies(); // Cookie 타입의 배열 생성후, 요청에 들어오온 쿠키들을 가져옴
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName()))  {
                return cookie;
            }
        }
        return null;
    }

    // 반환: 이름, 값, 만료 시간이 설정된 Cookie 객체
    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/"); // 쿠키 유효 범위 설정
        // HttpOnly를 켜 두면 자바스크립트가 세션 쿠키를 직접 읽지 못한다.
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}
