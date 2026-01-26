package com.petguardian.member.controller.login;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/front")
public class MemberLoginRedirectController {

    @GetMapping("/loginpage")
    public String login(){

        return "frontend/member/login/login";

    }

}
