package com.petguardian.pet.model;

import jakarta.persistence.*;

@Entity
@Table(name = "SERVICE_ITEMS") // 對接你的資料庫表名
public class PetServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SERVICE_ITEM_ID") // 對接你的 PK
    private Integer serviceItemId;

    @Column(name = "SERVICE_TYPE") // 對接服務項目名稱
    private String serviceType;

    @Column(name = "SERVICE_DETAIL")
    private String serviceDetail;

    @Column(name = "SERVICE_PRICE")
    private Integer servicePrice;
    
    @Column(name = "SERVICE_STATUS") // 補上這個，Repository 才能運作
    private Integer serviceStatus;
    
    // 必須與類別名稱完全一致的建構子
    public PetServiceItem() {}

    // Getter & Setter
    public Integer getServiceItemId() { return serviceItemId; }
    public void setServiceItemId(Integer serviceItemId) { this.serviceItemId = serviceItemId; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getServiceDetail() { return serviceDetail; }
    public void setServiceDetail(String serviceDetail) { this.serviceDetail = serviceDetail; }

    public Integer getServicePrice() { return servicePrice; }
    public void setServicePrice(Integer servicePrice) { this.servicePrice = servicePrice; }

    public Integer getServiceStatus() { return serviceStatus; }
    public void setServiceStatus(Integer serviceStatus) { this.serviceStatus = serviceStatus; }
    
}
