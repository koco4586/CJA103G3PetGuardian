package com.petguardian.member.controller.register;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/petguardian")
public class MemberRegisterRedirectController {

    @GetMapping("/register")
    public String register(){
    return "frontend/member/register/register";
    }
}