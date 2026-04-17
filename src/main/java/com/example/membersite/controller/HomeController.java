/*
 * 첫 화면("/") 요청을 처리하는 컨트롤러.
 * 들어온 요청에 세션 쿠키가 있는지 확인해서 이미 로그인한 사용자인지 검사한다.
 * 로그인 상태면 대시보드로 보내고, 아니면 홈 화면을 보여준다.
 */
package com.example.membersite.controller;

<<<<<<< HEAD
import com.example.membersite.support.SessionManager;
import jakarta.servlet.http.HttpServletRequest;
=======
import com.example.membersite.config.SessionConst;
import jakarta.servlet.http.HttpSession;
>>>>>>> 6926320 (nointercepter)
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final SessionManager sessionManager;

    public HomeController(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    // 첫 화면에서는 로그인 여부만 확인하고, 비로그인 사용자에게 홈 화면을 보여 준다.
    // 반환: 홈 화면 이름 또는 로그인 사용자의 대시보드 리다이렉트 경로
    @GetMapping("/")
<<<<<<< HEAD
    public String home(HttpServletRequest request) {
        String loginId = sessionManager.getLoginId(request);
=======
    public String home(Model model, HttpSession session) {
        String loginId = session == null ? null : (String) session.getAttribute(SessionConst.LOGIN_MEMBER);
>>>>>>> 6926320 (nointercepter)
        if (loginId != null) {
            return "redirect:/dashboard";
        }

<<<<<<< HEAD
        return "redirect:/login";
=======
        model.addAttribute("authenticated", false);
        model.addAttribute("loginId", null);
        return "home";
>>>>>>> 6926320 (nointercepter)
    }
}
