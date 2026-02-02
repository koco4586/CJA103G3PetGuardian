package com.petguardian.pet.model;

import java.io.Serializable;

public class PetserItemDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
    private Integer serviceItemId;     // 對應資料庫 PK
    private Integer sitterId;      // 關聯的保姆 ID
    private String serviceName;    // 服務名稱
    private String serviceDetail;  // 服務詳細內容
    private Integer servicePrice;  // 原始價格 (數字型態，方便運算)
    private Integer serviceStatus; // 狀態 (0: 下架, 1: 上架)

    public PetserItemDTO() {}

    // Getter and Setter
    public Integer getServiceId() { return serviceItemId; }
    public void setServiceId(Integer serviceId) { this.serviceItemId = serviceId; }

    public Integer getSitterId() { return sitterId; }
    public void setSitterId(Integer sitterId) { this.sitterId = sitterId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getServiceDetail() { return serviceDetail; }
    public void setServiceDetail(String serviceDetail) { this.serviceDetail = serviceDetail; }

    public Integer getServicePrice() { return servicePrice; }
    public void setServicePrice(Integer servicePrice) { this.servicePrice = servicePrice; }

    public Integer getServiceStatus() { return serviceStatus; }
    public void setServiceStatus(Integer serviceStatus) { this.serviceStatus = serviceStatus; }
}
