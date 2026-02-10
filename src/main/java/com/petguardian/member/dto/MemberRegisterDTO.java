package com.petguardian.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class MemberRegisterDTO {

	@NotBlank(message = "使用者姓名不得為空值或空白")
    private String memName;
	
	@NotBlank(message = "使用者電子信箱不得為空值或空白")
	@Email(message = "使用者電子信箱應為合法的信箱格式")
    private String memEmail;
	
	@NotBlank(message = "使用者帳號不得為空值或空白")
	@Size(min = 8, max = 16, message = "使用者帳號長度應介於8-16碼")
	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "使用者帳號應不含特殊符號") // ^ 表示字串開始+：表示可以有一個或多個符合的字元$表示字串結束
    private String memAcc;
	
	@NotBlank(message = "使用者密碼不得為空值或空白")
	@Size(min = 8, max = 16, message = "使用者密碼長度應介於8-16碼")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]+$", message = "使用者密碼設定應含英文大小寫+數字,不含特殊符號")
    private String memPw;
	
	@NotBlank(message = "使用者密碼確認不得為空值或空白")
	@Size(min = 8, max = 16, message = "使用者密碼確認長度應介於8-16碼")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]+$", message = "使用者密碼確認應含英文大小寫+數字,不含特殊符號")
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
