package com.petguardian.member.dto;

import java.time.LocalDate;

public class MemberManagementUpdateDTO {

    private String memName;

    private String memAcc;

    private String memUid;

    private LocalDate memBth;

    private Integer memSex;

    private String memEmail;

    private String memTel;

    private String memAdd;

    private String memAccountNumber;

    public String getMemName() {
        return memName;
    }

    public void setMemName(String memName) {
        this.memName = memName;
    }

    public String getMemAcc() {
        return memAcc;
    }

    public void setMemAcc(String memAcc) {
        this.memAcc = memAcc;
    }

    public String getMemUid() {
        return memUid;
    }

    public void setMemUid(String memUid) {
        this.memUid = memUid;
    }

    public LocalDate getMemBth() {
        return memBth;
    }

    public void setMemBth(LocalDate memBth) {
        this.memBth = memBth;
    }

    public Integer getMemSex() {
        return memSex;
    }

    public void setMemSex(Integer memSex) {
        this.memSex = memSex;
    }

    public String getMemEmail() {
        return memEmail;
    }

    public void setMemEmail(String memEmail) {
        this.memEmail = memEmail;
    }

    public String getMemTel() {
        return memTel;
    }

    public void setMemTel(String memTel) {
        this.memTel = memTel;
    }

    public String getMemAdd() {
        return memAdd;
    }

    public void setMemAdd(String memAdd) {
        this.memAdd = memAdd;
    }

    public String getMemAccountNumber() {
        return memAccountNumber;
    }

    public void setMemAccountNumber(String memAccountNumber) {
        this.memAccountNumber = memAccountNumber;
    }
}
