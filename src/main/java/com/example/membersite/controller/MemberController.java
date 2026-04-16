package com.example.membersite.controller;

import com.example.membersite.dto.UpdateNameForm;
import com.example.membersite.dto.UpdatePasswordForm;
import com.example.membersite.entity.Member;
import com.example.membersite.interceptor.LoginCheckInterceptor;
import com.example.membersite.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /*
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }
    */

    @GetMapping("/me")
    public String profile(HttpServletRequest request, Model model) {
        /*
         * previous way
         * String loginId = sessionManager.getLoginId(request);
         * if (loginId == null) {
         *     return "redirect:/login";
         * }
         */
        String loginId = getLoginId(request);
        Member member = memberService.findByLoginId(loginId);
        model.addAttribute("member", member);
        return "member/account";
    }

    @GetMapping("/me/name")
    public String editNameForm(HttpServletRequest request, Model model) {
        /*
         * previous way
         * String loginId = sessionManager.getLoginId(request);
         * if (loginId == null) {
         *     return "redirect:/login";
         * }
         */
        String loginId = getLoginId(request);
        Member member = memberService.findByLoginId(loginId);

        UpdateNameForm form = new UpdateNameForm();
        form.setName(member.getName());
        model.addAttribute("updateNameForm", form);
        return "member/edit-name";
    }

    @PostMapping("/me/name")
    public String editName(HttpServletRequest request,
                           @ModelAttribute UpdateNameForm updateNameForm,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        /*
         * previous way
         * String loginId = sessionManager.getLoginId(request);
         * if (loginId == null) {
         *     return "redirect:/login";
         * }
         */
        String loginId = getLoginId(request);

        if (updateNameForm.getName() == null || updateNameForm.getName().isBlank()) {
            model.addAttribute("nameError", "이름을 입력하세요.");
            return "member/edit-name";
        }

        if (updateNameForm.getName().length() > 30) {
            model.addAttribute("nameError", "이름은 30자 이하로 입력하세요.");
            return "member/edit-name";
        }

        memberService.updateName(loginId, updateNameForm.getName());
        redirectAttributes.addFlashAttribute("message", "Name updated.");
        return "redirect:/me";
    }

    @GetMapping("/me/password")
    public String editPasswordForm(Model model) {
        /*
         * previous way
         * if (sessionManager.getLoginId(request) == null) {
         *     return "redirect:/login";
         * }
         */
        model.addAttribute("updatePasswordForm", new UpdatePasswordForm());
        return "member/edit-password";
    }

    @PostMapping("/me/password")
    public String editPassword(HttpServletRequest request,
                               @ModelAttribute UpdatePasswordForm updatePasswordForm,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        /*
         * previous way
         * String loginId = sessionManager.getLoginId(request);
         * if (loginId == null) {
         *     return "redirect:/login";
         * }
         */
        String loginId = getLoginId(request);
        boolean hasError = false;

        if (updatePasswordForm.getCurrentPassword() == null || updatePasswordForm.getCurrentPassword().isBlank()) {
            model.addAttribute("currentPasswordError", "현재 비밀번호를 입력하세요.");
            hasError = true;
        }

        if (updatePasswordForm.getNewPassword() == null || updatePasswordForm.getNewPassword().isBlank()) {
            model.addAttribute("newPasswordError", "새 비밀번호를 입력하세요.");
            hasError = true;
        } else if (updatePasswordForm.getNewPassword().length() < 4
                || updatePasswordForm.getNewPassword().length() > 100) {
            model.addAttribute("newPasswordError", "새 비밀번호는 4자 이상 100자 이하로 입력하세요.");
            hasError = true;
        }

        if (updatePasswordForm.getNewPasswordConfirm() == null || updatePasswordForm.getNewPasswordConfirm().isBlank()) {
            model.addAttribute("newPasswordConfirmError", "새 비밀번호 확인을 입력하세요.");
            hasError = true;
        } else if (model.getAttribute("newPasswordError") == null && !updatePasswordForm.newPasswordMatches()) {
            model.addAttribute("newPasswordConfirmError", "비밀번호가 일치하지 않습니다.");
            hasError = true;
        }

        if (!hasError && !memberService.matchesPassword(loginId, updatePasswordForm.getCurrentPassword())) {
            model.addAttribute("currentPasswordError", "현재 비밀번호가 올바르지 않습니다.");
            hasError = true;
        }

        if (hasError) {
            return "member/edit-password";
        }

        memberService.updatePassword(loginId, updatePasswordForm.getNewPassword());
        redirectAttributes.addFlashAttribute("message", "Password updated.");
        return "redirect:/me";
    }

    private String getLoginId(HttpServletRequest request) {
        return (String) request.getAttribute(LoginCheckInterceptor.LOGIN_ID_ATTRIBUTE);
    }
}
