package com.petguardian.member.controller.login;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/front")
public class MemberLoginRedirectController {

    @GetMapping("/loginpage")
    public String loginpage(){

    	return "forward:/html/frontend/member/login/login.html";  //  改用 forward

    }

}