package com.petguardian.booking.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sitter_favorite") // 建議資料庫表名
public class BookingFavoriteVO implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sitter_fav_id")
    private Integer sitterFavId;

    @Column(name = "mem_id")
    private Integer memId;

    @Column(name = "sitter_id")
    private Integer sitterId;

    @Column(name = "fav_time", insertable = false, updatable = false)
    private LocalDateTime favTime;

    public Integer getMemId() {
        return memId;
    }

    public void setMemId(Integer memId) {
        this.memId = memId;
    }

    public Integer getSitterId() {
        return sitterId;
    }

    public void setSitterId(Integer sitterId) {
        this.sitterId = sitterId;
    }

    public Integer getSitterFavId() {
        return sitterFavId;
    }

    public void setSitterFavId(Integer sitterFavId) {
        this.sitterFavId = sitterFavId;
    }

    public LocalDateTime getFavTime() {
        return favTime;
    }

    public void setFavTime(LocalDateTime favTime) {
        this.favTime = favTime;
    }
}
