package com.example.membersite.controller;

import com.example.membersite.dto.UpdateNameForm;
import com.example.membersite.dto.UpdatePasswordForm;
import com.example.membersite.entity.Member;
import com.example.membersite.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/me")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Member member = memberService.findByLoginId(userDetails.getUsername());
        model.addAttribute("member", member);
        return "member/account";
    }

    @GetMapping("/me/name")
    public String editNameForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Member member = memberService.findByLoginId(userDetails.getUsername());
        UpdateNameForm form = new UpdateNameForm();
        form.setName(member.getName());
        model.addAttribute("updateNameForm", form);
        return "member/edit-name";
    }

    @PostMapping("/me/name")
    public String editName(@AuthenticationPrincipal UserDetails userDetails,
                           @Valid @ModelAttribute UpdateNameForm updateNameForm,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "member/edit-name";
        }

        memberService.updateName(userDetails.getUsername(), updateNameForm.getName());
        redirectAttributes.addFlashAttribute("message", "이름이 변경되었습니다.");
        return "redirect:/me";
    }

    @GetMapping("/me/password")
    public String editPasswordForm(Model model) {
        model.addAttribute("updatePasswordForm", new UpdatePasswordForm());
        return "member/edit-password";
    }

    @PostMapping("/me/password")
    public String editPassword(@AuthenticationPrincipal UserDetails userDetails,
                               @Valid @ModelAttribute UpdatePasswordForm updatePasswordForm,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasFieldErrors("newPassword")
                && !bindingResult.hasFieldErrors("newPasswordConfirm")
                && !updatePasswordForm.newPasswordMatches()) {
            bindingResult.rejectValue("newPasswordConfirm", "password.mismatch", "새 암호 확인이 일치하지 않습니다.");
        }

        if (!bindingResult.hasFieldErrors("currentPassword")
                && !memberService.matchesPassword(userDetails.getUsername(), updatePasswordForm.getCurrentPassword())) {
            bindingResult.rejectValue("currentPassword", "password.invalid", "현재 암호가 올바르지 않습니다.");
        }

        if (bindingResult.hasErrors()) {
            return "member/edit-password";
        }

        memberService.updatePassword(userDetails.getUsername(), updatePasswordForm.getNewPassword());
        redirectAttributes.addFlashAttribute("message", "암호가 변경되었습니다.");
        return "redirect:/me";
    }
}
