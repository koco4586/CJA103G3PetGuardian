package com.news.newstype;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "news_type")
@Getter @Setter
public class NewsType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_type_id")
    private Integer newsTypeId;

    @Column(name = "news_type_name", nullable = false, length = 20)
    private String newsTypeName;
}