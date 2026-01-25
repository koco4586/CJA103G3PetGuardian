package com.petguardian.admin.dto.insert;

public class AdminInsertDTO{

    private String admName;

    private String admAccount;

    private String admEmail;

    private String admTel;

    private String admPassword;

    private String admPasswordCheck;

    public String getAdmName() {
        return admName;
    }

    public void setAdmName(String admName) {
        this.admName = admName;
    }

    public String getAdmAccount() {
        return admAccount;
    }

    public void setAdmAccount(String admAccount) {
        this.admAccount = admAccount;
    }

    public String getAdmEmail() {
        return admEmail;
    }

    public void setAdmEmail(String admEmail) {
        this.admEmail = admEmail;
    }

    public String getAdmTel() {
        return admTel;
    }

    public void setAdmTel(String admTel) {
        this.admTel = admTel;
    }

    public String getAdmPassword() {
        return admPassword;
    }

    public void setAdmPassword(String admPassword) {
        this.admPassword = admPassword;
    }

    public String getAdmPasswordCheck() {
        return admPasswordCheck;
    }

    public void setAdmPasswordCheck(String admPasswordCheck) {
        this.admPasswordCheck = admPasswordCheck;
    }
}