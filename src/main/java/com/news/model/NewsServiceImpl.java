package com.news.model;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class NewsServiceImpl implements NewsService {

    @Autowired
    private NewsDAO newsDAO; // 注入 DAO 介面

    @Override
    public List<NewsVO> getAllNews() {
        // 使用 JpaRepository 內建的 findAll()
        return newsDAO.findAll();
    }

    @Override
    public List<NewsVO> getPublishedNews() {
        return newsDAO.findByIsPublishedOrderByCreatedTimeDesc(1);
    }

    @Override
    public NewsVO getNewsById(Integer id) {
        // findById 回傳的是 Optional，若找不到則回傳 null
        return newsDAO.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public NewsVO addNews(NewsVO news) {
        return newsDAO.save(news);
    }

    @Override
    @Transactional
    public NewsVO updateNews(Integer id, NewsVO news) {
        // 修改與新增共用 save，JPA 會根據 ID 自動判斷
        if (newsDAO.existsById(id)) {
            news.setNewsId(id);
            return newsDAO.save(news);
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteNews(Integer id) {
        // 根據 ID 刪除
        newsDAO.deleteById(id);
    }
}