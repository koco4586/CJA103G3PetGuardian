package com.petguardian.member.controller.register;

import com.petguardian.member.dto.MemberRegisterDTO;
import com.petguardian.member.service.register.MemberRegisterService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/front")
public class MemberRegisterController {

    @Autowired
    private MemberRegisterService memberRegisterService;

    @PostMapping("/register")
    public Map<String,String> register(@RequestBody @Valid MemberRegisterDTO memberRegisterDTO){

        Map<String,String> map = new HashMap<>();

        String result = memberRegisterService.register(memberRegisterDTO);

        map.put("result", result);

        return map;

    }

}
