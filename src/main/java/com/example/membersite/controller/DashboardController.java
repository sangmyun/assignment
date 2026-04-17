/*
 * 로그인 후 보는 대시보드 화면을 처리하는 컨트롤러.
 * 요청 쿠키에서 loginId를 찾고, 그 loginId로 회원 정보를 조회해서 화면에 전달한다.
 * 로그인 상태가 아니면 로그인 페이지로 이동시킨다.
 */
package com.example.membersite.controller;

import com.example.membersite.config.SessionConst;
import com.example.membersite.entity.Member;
import com.example.membersite.service.MemberService;
<<<<<<< HEAD
import com.example.membersite.support.SessionManager;
import jakarta.servlet.http.HttpServletRequest;
=======
import jakarta.servlet.http.HttpSession;
>>>>>>> 6926320 (nointercepter)
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final MemberService memberService;
    private final SessionManager sessionManager;

    public DashboardController(MemberService memberService, SessionManager sessionManager) {
        this.memberService = memberService;
        this.sessionManager = sessionManager;
    }

    // 세션에서 loginId를 얻고, 그 값으로 회원 정보를 조회해 화면에 전달한다.
    // 반환: 대시보드 화면 이름 또는 비로그인 사용자의 로그인 화면 리다이렉트 경로
    @GetMapping("/dashboard")
<<<<<<< HEAD
    public String dashboard(HttpServletRequest request, Model model) {
        String loginId = sessionManager.getLoginId(request);
        if (loginId == null) {
            return "redirect:/login";
        }

        Member member = memberService.findByLoginId(loginId);
        model.addAttribute("m", member);
=======
    public String dashboard(HttpSession session, Model model) {
        String loginId = (String) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Member member = memberService.findByLoginId(loginId);
        model.addAttribute("member", member);
>>>>>>> 6926320 (nointercepter)
        return "dashboard/index";
    }
}
