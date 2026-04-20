package com.example.membersite.controller;

import com.example.membersite.dto.UpdateNameForm;
import com.example.membersite.dto.UpdatePasswordForm;
import com.example.membersite.entity.Member;
import com.example.membersite.interceptor.LoginCheckInterceptor;
import com.example.membersite.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public String profile(HttpServletRequest request, Model model) {
        String loginId = getLoginId(request);
        Member member = memberService.findByLoginId(loginId);
        model.addAttribute("member", member);
        return "member/account";
    }

    @GetMapping("/me/name")
    public String editNameForm(HttpServletRequest request, Model model) {
        String loginId = getLoginId(request);
        Member member = memberService.findByLoginId(loginId);

        UpdateNameForm form = new UpdateNameForm();
        form.setName(member.getName());
        model.addAttribute("updateNameForm", form);
        return "member/edit-name";
    }

    @PostMapping("/me/name")
    public String editName(HttpServletRequest request,
                           @Valid @ModelAttribute UpdateNameForm updateNameForm,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        String loginId = getLoginId(request);
        if (bindingResult.hasErrors()) {
            return "member/edit-name";
        }

        memberService.updateName(loginId, updateNameForm.getName());
        redirectAttributes.addFlashAttribute("message", "Name updated.");
        return "redirect:/me";
    }

    @GetMapping("/me/password")
    public String editPasswordForm(Model model) {
        model.addAttribute("updatePasswordForm", new UpdatePasswordForm());
        return "member/edit-password";
    }

    @PostMapping("/me/password")
    public String editPassword(HttpServletRequest request,
                               @Valid @ModelAttribute UpdatePasswordForm updatePasswordForm,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        String loginId = getLoginId(request);

        if (!bindingResult.hasFieldErrors("newPassword")
                && !bindingResult.hasFieldErrors("newPasswordConfirm")
                && !updatePasswordForm.newPasswordMatches()) {
            bindingResult.rejectValue("newPasswordConfirm", "mismatch", "비밀번호가 일치하지 않습니다.");
        }

        if (!bindingResult.hasFieldErrors("currentPassword")
                && !memberService.matchesPassword(loginId, updatePasswordForm.getCurrentPassword())) {
            bindingResult.rejectValue("currentPassword", "mismatch", "현재 비밀번호가 올바르지 않습니다.");
        }

        if (bindingResult.hasErrors()) {
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
