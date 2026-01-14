package com.news.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "news")
@Getter @Setter
public class News {


@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer newsId;


private String title;
private String content;
private Integer status; // 0=未發布, 1=已發布


@Column(name = "created_time")
private LocalDateTime createdTime;
}