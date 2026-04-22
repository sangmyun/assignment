package com.example.membersite.controller;

import com.example.membersite.dto.SignupForm;
import com.example.membersite.service.MemberService;
import com.example.membersite.support.AuthTokenManager;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;
    private final AuthTokenManager authTokenManager;

    // model은 컨트롤러가 템플릿에 넘겨주는 데이터, 템플릿에서 ${signupForm}으로 꺼내씀
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupForm", new SignupForm());
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute SignupForm signupForm, Model model) {
        boolean hasError = false;

        if (signupForm.getLoginId() == null || signupForm.getLoginId().isBlank()) {
            model.addAttribute("loginIdError", "로그인 아이디를 입력하세요.");
            hasError = true;
        } else if (signupForm.getLoginId().length() < 4 || signupForm.getLoginId().length() > 20) {
            model.addAttribute("loginIdError", "로그인 아이디는 4자 이상 20자 이하로 입력하세요.");
            hasError = true;
        }

        if (signupForm.getPassword() == null || signupForm.getPassword().isBlank()) {
            model.addAttribute("passwordError", "비밀번호를 입력하세요.");
            hasError = true;
        } else if (signupForm.getPassword().length() < 4 || signupForm.getPassword().length() > 100) {
            model.addAttribute("passwordError", "비밀번호는 4자 이상 입력하세요.");
            hasError = true;
        }

        if (signupForm.getPasswordConfirm() == null || signupForm.getPasswordConfirm().isBlank()) {
            model.addAttribute("passwordConfirmError", "비밀번호 확인을 입력하세요.");
            hasError = true;
        } else if (model.getAttribute("passwordError") == null && !signupForm.passwordMatches()) {
            model.addAttribute("passwordConfirmError", "비밀번호가 일치하지 않습니다.");
            hasError = true;
        }

        if (signupForm.getName() == null || signupForm.getName().isBlank()) {
            model.addAttribute("nameError", "이름을 입력하세요.");
            hasError = true;
        } else if (signupForm.getName().length() > 30) {
            model.addAttribute("nameError", "이름은 30자 이하로 입력하세요.");
            hasError = true;
        }

        if (!hasError && memberService.isDuplicatedLoginId(signupForm.getLoginId())) {
            model.addAttribute("loginIdError", "이미 사용 중인 로그인 아이디입니다.");
            hasError = true;
        }

        if (hasError) {
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
