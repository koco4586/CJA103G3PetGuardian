//package com.petguardian.admin.model;
//
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "admin")
//public class Admin {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "adm_id")
//    private Integer admId;
//
//    @Column(name = "adm_account")
//    private String admAccount;
//
//    @Column(name = "adm_password")
//    private String admPassword;
//
//    @Column(name = "adm_name")
//    private String admName;
//
//    @Column(name = "adm_email")
//    private String admEmail;
//
//    @Column(name = "adm_image")
//    private String admImage;
//
//    @Column(name = "adm_tel")
//    private String admTel;
//
//    @Column(name = "adm_status")
//    private Integer admStatus;
//
//    @Column(name = "adm_login_attempts")
//    private Integer admLoginAttempts;
//
//    @Column(name = "adm_created_at")
//    private LocalDateTime admCreatedAt;
//
//    // Getters and Setters
//
//    public Integer getAdmId() {
//        return admId;
//    }
//
//    public void setAdmId(Integer admId) {
//        this.admId = admId;
//    }
//
//    public String getAdmAccount() {
//        return admAccount;
//    }
//
//    public void setAdmAccount(String admAccount) {
//        this.admAccount = admAccount;
//    }
//
//    public String getAdmPassword() {
//        return admPassword;
//    }
//
//    public void setAdmPassword(String admPassword) {
//        this.admPassword = admPassword;
//    }
//
//    public String getAdmName() {
//        return admName;
//    }
//
//    public void setAdmName(String admName) {
//        this.admName = admName;
//    }
//
//    public String getAdmEmail() {
//        return admEmail;
//    }
//
//    public void setAdmEmail(String admEmail) {
//        this.admEmail = admEmail;
//    }
//
//    public String getAdmImage() {
//        return admImage;
//    }
//
//    public void setAdmImage(String admImage) {
//        this.admImage = admImage;
//    }
//
//    public String getAdmTel() {
//        return admTel;
//    }
//
//    public void setAdmTel(String admTel) {
//        this.admTel = admTel;
//    }
//
//    public Integer getAdmStatus() {
//        return admStatus;
//    }
//
//    public void setAdmStatus(Integer admStatus) {
//        this.admStatus = admStatus;
//    }
//
//    public Integer getAdmLoginAttempts() {
//        return admLoginAttempts;
//    }
//
//    public void setAdmLoginAttempts(Integer admLoginAttempts) {
//        this.admLoginAttempts = admLoginAttempts;
//    }
//
//    public LocalDateTime getAdmCreatedAt() {
//        return admCreatedAt;
//    }
//
//    public void setAdmCreatedAt(LocalDateTime admCreatedAt) {
//        this.admCreatedAt = admCreatedAt;
//    }
//}
