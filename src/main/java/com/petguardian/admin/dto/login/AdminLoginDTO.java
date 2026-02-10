package com.petguardian.admin.dto.login;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AdminLoginDTO {

	@NotBlank(message = "管理員帳號不得為空值或空白")
	@Size(min = 8, max = 16, message = "管理員帳號長度應介於8-16碼")
	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "管理員帳號應不含特殊符號") // ^ 表示字串開始+：表示可以有一個或多個符合的字元$表示字串結束
	private String admAccount;

	@NotBlank(message = "管理員密碼不得為空值或空白")
	@Size(min = 8, max = 16, message = "管理員密碼長度應介於8-16碼")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]+$", message = "管理員密碼設定應含英文大小寫+數字,不含特殊符號")
	private String admPassword;

	public String getAdmAccount() {
		return admAccount;
	}

	public void setAdmAccount(String admAccount) {
		this.admAccount = admAccount;
	}

	public String getAdmPassword() {
		return admPassword;
	}

	public void setAdmPassword(String admPassword) {
		this.admPassword = admPassword;
	}

}
