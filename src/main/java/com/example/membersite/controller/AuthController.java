package com.example.membersite.controller;

import com.example.membersite.dto.SignupForm;
import com.example.membersite.service.MemberService;
import java.security.Principal;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final MemberService memberService;

    public AuthController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/signup")
    public String signupForm(Model model, Principal principal) {
        if (principal != null) {
            return "redirect:/me";
        }

        model.addAttribute("signupForm", new SignupForm());
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute SignupForm signupForm,
                         BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("password")
                && !bindingResult.hasFieldErrors("passwordConfirm")
                && !signupForm.passwordMatches()) {
            bindingResult.rejectValue("passwordConfirm", "password.mismatch", "암호 확인이 일치하지 않습니다.");
        }

        if (!bindingResult.hasFieldErrors("loginId")
                && memberService.isDuplicatedLoginId(signupForm.getLoginId())) {
            bindingResult.rejectValue("loginId", "loginId.duplicate", "이미 사용 중인 로그인 아이디입니다.");
        }

        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        memberService.register(signupForm);
        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String login(Principal principal) {
        if (principal != null) {
            return "redirect:/me";
        }
        return "auth/login";
    }
}
