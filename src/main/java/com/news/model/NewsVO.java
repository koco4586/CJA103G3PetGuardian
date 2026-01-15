package com.news.model;

//import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "news")
@Getter @Setter
public class NewsVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id")
    private Integer newsId;

    @Column(name = "adm_id")
    private Integer admId;

    @Column(name = "news_type_id")
    private Integer newsTypeId;

    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_published")
    private Integer isPublished; // 0=未發布, 1=已發布

    @Column(name = "created_time", insertable = false, updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time", insertable = false, updatable = false)
    private LocalDateTime updatedTime;
    
    @Column(name = "publish_date")
    private LocalDateTime publishDate;
}