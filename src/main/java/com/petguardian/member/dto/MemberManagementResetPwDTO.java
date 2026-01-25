package com.petguardian.member.dto;

public class MemberManagementResetPwDTO {

    private String memPw;
    private String memPwCheck;

    public String getMemPw() {
        return memPw;
    }

    public void setMemPw(String memPw) {
        this.memPw = memPw;
    }

    public String getMemPwCheck() {
        return memPwCheck;
    }

    public void setMemPwCheck(String memPwCheck) {
        this.memPwCheck = memPwCheck;
    }
}
