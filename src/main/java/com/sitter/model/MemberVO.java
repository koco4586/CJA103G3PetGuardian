package com.sitter.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "member")
public class MemberVO {
	public MemberVO() {};
	
	@Id
	@Column(name = "mem_id", updatable = false, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer memId;	//會員編號	int
	
	
	@Column(name ="mem_name",  nullable = false, length = 10)
	private String memName;	//會員姓名	varchar
	
	@Column(name ="mem_add")
	private String memAdd;	//會員地址	varchar

	public Integer getMemId() {
		return memId;
	}

	public void setMemId(Integer memId) {
		this.memId = memId;
	}

	public String getMemName() {
		return memName;
	}

	public void setMemName(String memName) {
		this.memName = memName;
	}

	public String getMemAdd() {
		return memAdd;
	}

	public void setMemAdd(String memAdd) {
		this.memAdd = memAdd;
	}
	
	
}
