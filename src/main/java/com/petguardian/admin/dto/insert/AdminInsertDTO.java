package com.petguardian.admin.dto.insert;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AdminInsertDTO {

	@NotBlank(message = "管理員姓名不得為空值或空白")
	private String admName;

	@NotBlank(message = "管理員帳號不得為空值或空白")
	@Size(min = 8, max = 16, message = "管理員帳號長度應介於8-16碼")
	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "管理員帳號應不含特殊符號") // ^ 表示字串開始+：表示可以有一個或多個符合的字元$表示字串結束
	private String admAccount;

	@NotBlank(message = "管理員電子信箱不得為空值或空白")
	@Email(message = "管理員電子信箱應為合法的信箱格式")
	private String admEmail;

	@NotBlank(message = "管理員電話不得為空值或空白")
	private String admTel;

	@NotBlank(message = "管理員密碼不得為空值或空白")
	@Size(min = 8, max = 16, message = "管理員密碼長度應介於8-16碼")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]+$", message = "管理員密碼設定應含英文大小寫+數字,不含特殊符號")
	private String admPassword;

	@NotBlank(message = "管理員密碼確認不得為空值或空白")
	@Size(min = 8, max = 16, message = "管理員密碼確認長度應介於8-16碼")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]+$", message = "管理員密碼確認應含英文大小寫+數字,不含特殊符號")
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