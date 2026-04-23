package com.example.membersite.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /**
     * Redirects the root path to the login page.
     *
     * @return redirect view name
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
}
