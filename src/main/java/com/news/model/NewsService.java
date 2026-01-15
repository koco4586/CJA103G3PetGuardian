package com.news.model;

import java.util.List;
import com.news.model.*;

public interface NewsService {
    List<NewsVO> getAllNews();           // 查詢所有
     List<NewsVO> getPublishedNews(); // 供前台使用
    NewsVO getNewsById(Integer id);      // 根據 ID 查詢
    NewsVO addNews(NewsVO news);           // 新增消息
    NewsVO updateNews(Integer id, NewsVO news);        // 編輯消息
    void deleteNews(Integer id);       // 刪除消息
}
