package com.example.membersite.controller;

import com.example.membersite.entity.Member;
import com.example.membersite.interceptor.LoginCheckInterceptor;
import com.example.membersite.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final MemberService memberService;

    /**
     * Renders the dashboard page for the authenticated user.
     *
     * @param request servlet request
     * @param model view model
     * @return dashboard view name
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        String loginId = (String) request.getAttribute(LoginCheckInterceptor.LOGIN_ID_ATTRIBUTE);
        Member member = memberService.findByLoginId(loginId);
        model.addAttribute("m", member);
        return "dashboard/index";
    }
}
