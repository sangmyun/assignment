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

    /*
    public DashboardController(MemberService memberService) {
        this.memberService = memberService;
    }
    */

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        /*
         * previous way
         * String loginId = sessionManager.getLoginId(request);
         * if (loginId == null) {
         *     return "redirect:/login";
         * }
         */


        // dashboard로 요청이 들어오면 WebConifg 파일을 참고해서 LonginCehckInterceptor 클래스의 preHandle() 메소드가 검사를 함. 이때 preHandle() 메소드는 스프링에서 자동으로 실행해줌
        // 요청 객체에서 key가 login인 값을 가져와 string으로 캐스팅
        String loginId = (String) request.getAttribute(LoginCheckInterceptor.LOGIN_ID_ATTRIBUTE);//loginId
        Member member = memberService.findByLoginId(loginId);
        model.addAttribute("m", member);
        return "dashboard/index";
    }
}
