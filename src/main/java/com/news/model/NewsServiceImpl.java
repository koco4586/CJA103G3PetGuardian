package com.news.model;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NewsServiceImpl implements NewsService {

    @Autowired
    private NewsDAO newsDAO; // 注入 DAO 介面

    @Override
    public List<News> getAllNews() {
        // 使用 JpaRepository 內建的 findAll()
        return newsDAO.findAll();
    }

    @Override
    public News getNewsById(Integer id) {
        // findById 回傳的是 Optional，若找不到則回傳 null
        return newsDAO.findById(id).orElse(null);
    }

    @Override
    public News addNews(News news) {
        // save 方法可用於新增，也可用於修改
        return newsDAO.save(news);
    }

    @Override
    public News updateNews(News news) {
        // 修改與新增共用 save，JPA 會根據 ID 自動判斷
        return newsDAO.save(news);
    }

    @Override
    public void deleteNews(Integer id) {
        // 根據 ID 刪除
        newsDAO.deleteById(id);
    }
}