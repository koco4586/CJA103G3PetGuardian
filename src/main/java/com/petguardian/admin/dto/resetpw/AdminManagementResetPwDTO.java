package com.petguardian.admin.dto.resetpw;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AdminManagementResetPwDTO {

	@NotBlank(message = "管理員密碼不得為空值或空白")
	@Size(min = 8, max = 16, message = "管理員密碼長度應介於8-16碼")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]+$", message = "管理員密碼設定應含英文大小寫+數字,不含特殊符號")
	private String admPassword;

	@NotBlank(message = "管理員密碼確認不得為空值或空白")
	@Size(min = 8, max = 16, message = "管理員密碼確認長度應介於8-16碼")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]+$", message = "管理員密碼確認應含英文大小寫+數字,不含特殊符號")
	private String admPasswordCheck;

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
