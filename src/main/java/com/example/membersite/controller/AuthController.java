package com.example.membersite.controller;

import com.example.membersite.dto.SignupForm;
import com.example.membersite.service.MemberService;
import com.example.membersite.support.AuthTokenManager;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;
    private final AuthTokenManager authTokenManager;

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupForm", new SignupForm());
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute SignupForm signupForm, BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("password")
                && !bindingResult.hasFieldErrors("passwordConfirm")
                && !signupForm.passwordMatches()) {
            bindingResult.rejectValue("passwordConfirm", "mismatch", "비밀번호가 일치하지 않습니다.");
        }

        if (!bindingResult.hasFieldErrors("loginId")
                && memberService.isDuplicatedLoginId(signupForm.getLoginId())) {
            bindingResult.rejectValue("loginId", "duplicated", "이미 사용 중인 로그인 아이디입니다.");
        }

        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        memberService.register(signupForm);
        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String loginId,
                        @RequestParam String password,
                        HttpServletResponse response) {
        if (!memberService.authenticate(loginId, password)) {
            return "redirect:/login?error";
        }

        authTokenManager.createToken(loginId, response);
        return "redirect:/dashboard";
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        authTokenManager.expire(response);
        return "redirect:/login?logout";
    }
}
