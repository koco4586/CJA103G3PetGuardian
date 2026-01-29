package com.petguardian.news.service;

import com.petguardian.news.model.News;
import com.petguardian.news.model.NewsType;
import java.util.List;
import java.util.Optional;

public interface NewsService {

    // 查詢所有已發布的消息（前台用）
    List<News> getAllPublishedNews();

    // 查詢所有消息（後台用）
    List<News> getAllNews();

    // 根據ID查詢單則消息
    Optional<News> getNewsById(Integer id);

    // 新增或更新消息
    News saveNews(News news);

    // 刪除消息
    void deleteNews(Integer id);

    // 查詢所有消息類別
    List<NewsType> getAllNewsTypes();

    // 根據類別ID查詢消息類別
    Optional<NewsType> getNewsTypeById(Integer id);
}