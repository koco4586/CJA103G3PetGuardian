package com.petguardian.news.service;

import com.petguardian.news.model.News;
import com.petguardian.news.model.NewsRepository;
import com.petguardian.news.model.NewsType;
import com.petguardian.news.model.NewsTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NewsServiceImpl implements NewsService {

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private NewsTypeRepository newsTypeRepository;

    @Override
    public List<News> getAllPublishedNews() {
        // 只回傳已發布的消息（isPublished = 1）
        return newsRepository.findByIsPublishedOrderByPublishDateDesc(1);
    }

    @Override
    public List<News> getAllNews() {
        // 回傳所有消息（後台管理用）
        return newsRepository.findAll();
    }

    @Override
    public Optional<News> getNewsById(Integer id) {
        return newsRepository.findById(id);
    }

    @Override
    public News saveNews(News news) {
        return newsRepository.save(news);
    }

    @Override
    public void deleteNews(Integer id) {
        newsRepository.deleteById(id);
    }

    @Override
    public List<NewsType> getAllNewsTypes() {
        return newsTypeRepository.findAll();
    }

    @Override
    public Optional<NewsType> getNewsTypeById(Integer id) {
        return newsTypeRepository.findById(id);
    }
}