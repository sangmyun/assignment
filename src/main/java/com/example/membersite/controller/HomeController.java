package com.example.membersite.controller;

import com.example.membersite.support.SessionManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SessionManager sessionManager;

    /*
    public HomeController(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
    */

    @GetMapping("/")
    public String home(HttpServletRequest request) {
        String loginId = sessionManager.getLoginId(request);
        if (loginId != null) {
            return "redirect:/dashboard";
        }

        return "redirect:/login";
    }
}
