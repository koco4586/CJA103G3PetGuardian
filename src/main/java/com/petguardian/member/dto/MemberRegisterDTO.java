package com.petguardian.member.dto;

public class MemberRegisterDTO {

    private String memName;
    private String memEmail;
    private String memAcc;
    private String memPw;
    private String memPwCheck;

    public String getMemName() {
        return memName;
    }

    public void setMemName(String memName) {
        this.memName = memName;
    }

    public String getMemEmail() {
        return memEmail;
    }

    public void setMemEmail(String memEmail) {
        this.memEmail = memEmail;
    }

    public String getMemAcc() {
        return memAcc;
    }

    public void setMemAcc(String memAcc) {
        this.memAcc = memAcc;
    }

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
