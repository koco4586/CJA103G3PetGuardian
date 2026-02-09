package com.petguardian.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class MemberManagementResetPwDTO {

	@NotBlank(message = "使用者密碼不得為空值或空白")
	@Size(min = 8, max = 16, message = "使用者密碼長度應介於8-16碼")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]+$", message = "使用者密碼設定應含英文大小寫+數字,不含特殊符號")
	private String memPw;

	@NotBlank(message = "使用者密碼確認不得為空值或空白")
	@Size(min = 8, max = 16, message = "使用者密碼確認長度應介於8-16碼")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]+$", message = "使用者密碼確認應含英文大小寫+數字,不含特殊符號")
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
