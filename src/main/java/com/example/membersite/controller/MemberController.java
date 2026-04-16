/*
 * 내 정보 조회, 이름 수정, 비밀번호 수정 화면을 처리하는 컨트롤러.
 * 모든 기능은 먼저 세션 쿠키를 확인해서 로그인한 사용자인지 검사한다.
 * 로그인한 사용자의 loginId를 기준으로 MemberService에 작업을 요청한다.
 */
package com.example.membersite.controller;

import com.example.membersite.dto.UpdateNameForm;
import com.example.membersite.dto.UpdatePasswordForm;
import com.example.membersite.entity.Member;
import com.example.membersite.service.MemberService;
import com.example.membersite.support.SessionManager;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MemberController {

    private final MemberService memberService;
    private final SessionManager sessionManager;

    public MemberController(MemberService memberService, SessionManager sessionManager) {
        this.memberService = memberService;
        this.sessionManager = sessionManager;
    }

    // 반환: 내 정보 화면 이름 또는 비로그인 사용자의 로그인 화면 리다이렉트 경로
    @GetMapping("/me")
    public String profile(HttpServletRequest request, Model model) {
        String loginId = sessionManager.getLoginId(request);
        if (loginId == null) {
            return "redirect:/login";
        }

        Member member = memberService.findByLoginId(loginId);
        model.addAttribute("member", member);
        return "member/account";
    }

    // 반환: 이름 수정 화면 이름 또는 비로그인 사용자의 로그인 화면 리다이렉트 경로
    @GetMapping("/me/name")
    public String editNameForm(HttpServletRequest request, Model model) {
        String loginId = sessionManager.getLoginId(request);
        if (loginId == null) {
            return "redirect:/login";
        }

        Member member = memberService.findByLoginId(loginId);
        UpdateNameForm form = new UpdateNameForm();
        form.setName(member.getName());
        model.addAttribute("updateNameForm", form);
        return "member/edit-name";
    }

    /*
     * 이름 수정 폼을 직접 검사하는 함수.
     * 이름이 비었는지, 길이가 너무 긴지 if 문으로 확인하고,
     * 오류가 있으면 모델에 메시지를 담아 다시 수정 화면을 보여준다.
     */
    // 반환: 검증 실패 시 이름 수정 화면 이름, 성공 시 내 정보 화면 리다이렉트 경로
    @PostMapping("/me/name")
    public String editName(HttpServletRequest request,
                           @ModelAttribute UpdateNameForm updateNameForm,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        String loginId = sessionManager.getLoginId(request);
        if (loginId == null) {
            return "redirect:/login";
        }

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

    // 반환: 비밀번호 수정 화면 이름 또는 비로그인 사용자의 로그인 화면 리다이렉트 경로
    @GetMapping("/me/password")
    public String editPasswordForm(HttpServletRequest request, Model model) {
        if (sessionManager.getLoginId(request) == null) {
            return "redirect:/login";
        }

        model.addAttribute("updatePasswordForm", new UpdatePasswordForm());
        return "member/edit-password";
    }

    /*
     * 비밀번호 수정 폼을 직접 검사하는 함수.
     * 현재 비밀번호 입력 여부, 새 비밀번호 길이, 확인 비밀번호 일치 여부를 순서대로 검사한다.
     * 모든 검사를 통과하면 실제 비밀번호를 변경한다.
     */
    // 반환: 검증 실패 시 비밀번호 수정 화면 이름, 성공 시 내 정보 화면 리다이렉트 경로
    @PostMapping("/me/password")
    public String editPassword(HttpServletRequest request,
                               @ModelAttribute UpdatePasswordForm updatePasswordForm,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        String loginId = sessionManager.getLoginId(request);
        if (loginId == null) {
            return "redirect:/login";
        }

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
            model.addAttribute("newPasswordError", "새 비밀번호는 4자 이상 입력하세요.");
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
}
