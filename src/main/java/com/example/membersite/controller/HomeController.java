package com.example.membersite.controller;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model, Principal principal) {
        if (principal != null) {
            return "redirect:/dashboard";
        }

        model.addAttribute("authenticated", principal != null);
        model.addAttribute("loginId", principal != null ? principal.getName() : null);
        return "home";
    }
}
