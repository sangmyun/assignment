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

    /**
     * Renders the account page.
     *
     * @param request servlet request
     * @param model view model
     * @return account view name
     */
    @GetMapping("/me")
    public String profile(HttpServletRequest request, Model model) {
        String loginId = getLoginId(request);
        Member member = memberService.findByLoginId(loginId);
        model.addAttribute("member", member);
        return "member/account";
    }

    /**
     * Renders the name edit page prefilled with the current name.
     *
     * @param request servlet request
     * @param model view model
     * @return edit-name view name
     */
    @GetMapping("/me/name")
    public String editNameForm(HttpServletRequest request, Model model) {
        String loginId = getLoginId(request);
        Member member = memberService.findByLoginId(loginId);

        UpdateNameForm form = new UpdateNameForm();
        form.setName(member.getName());
        model.addAttribute("updateNameForm", form);
        return "member/edit-name";
    }

    /**
     * Updates the member name after validation.
     *
     * @param request servlet request
     * @param updateNameForm submitted name form
     * @param bindingResult validation result
     * @param redirectAttributes redirect attributes
     * @return edit-name view on error, otherwise profile redirect
     */
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

    /**
     * Renders the password edit page.
     *
     * @param model view model
     * @return edit-password view name
     */
    @GetMapping("/me/password")
    public String editPasswordForm(Model model) {
        model.addAttribute("updatePasswordForm", new UpdatePasswordForm());
        return "member/edit-password";
    }

    /**
     * Updates the password after matching and current-password checks.
     *
     * @param request servlet request
     * @param updatePasswordForm submitted password form
     * @param bindingResult validation result
     * @param redirectAttributes redirect attributes
     * @return edit-password view on error, otherwise profile redirect
     */
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

    /**
     * Reads login id attached by the login-check interceptor.
     *
     * @param request servlet request
     * @return authenticated login id
     */
    private String getLoginId(HttpServletRequest request) {
        return (String) request.getAttribute(LoginCheckInterceptor.LOGIN_ID_ATTRIBUTE);
    }
}
