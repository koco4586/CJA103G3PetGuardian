package com.news.model;

import java.util.List;

import com.news.model.News;

public interface NewsService {
    List<News> getAllNews();           // 查詢所有
    News getNewsById(Integer id);      // 根據 ID 查詢
    News addNews(News news);           // 新增消息
    News updateNews(News news);        // 編輯消息
    void deleteNews(Integer id);       // 刪除消息
}
