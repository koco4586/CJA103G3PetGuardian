package com.petguardian.news.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Getter
@Setter
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id")
    private Integer newsId;

    @Column(name = "adm_id", nullable = false)
    private Integer admId; // 發布的管理員ID

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    @Column(name = "publish_date")
    private LocalDateTime publishDate;

    @Column(name = "is_published", nullable = false)
    private Integer isPublished = 0; // 0=草稿, 1=已發布

    // 多對一關聯 - 直接使用物件關聯
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "news_type_id", nullable = false)
    private NewsType newsType;

    // JPA 生命週期：新增時自動設定時間
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
    }

    // JPA 生命週期：更新時自動修改時間
    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}