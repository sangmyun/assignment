package com.example.membersite.controller;

import com.example.membersite.entity.Member;
import com.example.membersite.service.MemberService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final MemberService memberService;

    public DashboardController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Member member = memberService.findByLoginId(userDetails.getUsername());
        model.addAttribute("member", member);
        return "dashboard/index";
    }
}
