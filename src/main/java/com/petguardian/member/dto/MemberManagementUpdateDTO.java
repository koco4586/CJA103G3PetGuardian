package com.petguardian.member.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class MemberManagementUpdateDTO {

	@NotBlank(message = "使用者姓名不得為空值或空白")
	private String memName;

	@NotBlank(message = "使用者帳號不得為空值或空白")
	@Size(min = 8, max = 16, message = "使用者帳號長度應介於8-16碼")
	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "使用者帳號應不含特殊符號") // ^ 表示字串開始+：表示可以有一個或多個符合的字元$表示字串結束
	private String memAcc;

	@Pattern(regexp = "^[A-Z][12]\\d{8}$", message = "身分證字號格式不正確")
	private String memUid;

	@Past(message = "使用者出生日期應小於當前日期")
	private LocalDate memBth;

	private Integer memSex;

	@NotBlank(message = "使用者電子信箱不得為空值或空白")
	@Email(message = "使用者電子信箱應為合法的信箱格式")
	private String memEmail;

	@Pattern(regexp = "^09\\d{8}$", message = "使用者手機號碼格式有誤")
	private String memTel;

	private String memAdd;

	@Pattern(regexp = "^\\d{10,16}$", message = "銀行帳號格式應為10-16碼阿拉伯數字")
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
