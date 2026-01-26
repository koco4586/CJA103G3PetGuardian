package com.petguardian.news.service;

import com.petguardian.news.model.News;
import com.petguardian.news.model.NewsRepository;
import com.petguardian.news.model.NewsType;
import com.petguardian.news.model.NewsTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class NewsServiceImpl implements NewsService {

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private NewsTypeRepository newsTypeRepository;

    @Override
    public List<News> getAllPublishedNews() {
        // 只回傳已發布的消息，按發布日期降冪排序
        return newsRepository.findByIsPublishedOrderByPublishDateDesc(1);
    }

    @Override
    public List<News> getAllNews() {
        // 回傳所有消息，按更新時間降冪排序
        return newsRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedTime"));
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

    @Override
    public List<News> searchNews(Integer newsTypeId, String keyword) {
        // 分類
        if (newsTypeId != null && newsTypeId > 0 && (keyword == null || keyword.trim().isEmpty())) {
            return newsRepository.findByNewsType_NewsTypeIdAndIsPublishedOrderByPublishDateDesc(newsTypeId, 1);
        }

        // 關鍵字
        if ((newsTypeId == null || newsTypeId == 0) && keyword != null && !keyword.trim().isEmpty()) {
            String cleanKeyword = keyword.trim();

            // 搜尋標題或內容包含關鍵字的消息
            List<News> result = newsRepository
                    .findByIsPublishedAndTitleContainingOrIsPublishedAndContentContainingOrderByPublishDateDesc(
                            1, cleanKeyword, 1, cleanKeyword);

            // 去除重複的消息，使用 Set 來去重
            Set<Integer> newsIds = new HashSet<>();
            List<News> uniqueNews = new ArrayList<>();
            for (News news : result) {
                if (!newsIds.contains(news.getNewsId())) {
                    newsIds.add(news.getNewsId());
                    uniqueNews.add(news);
                }
            }

            return uniqueNews;
        }

        // 分類+關鍵字
        if (newsTypeId != null && newsTypeId > 0 && keyword != null && !keyword.trim().isEmpty()) {
            String cleanKeyword = keyword.trim();

            // 搜尋特定分類中，標題包含關鍵字的消息
            List<News> titleResults = newsRepository
                    .findByNewsType_NewsTypeIdAndIsPublishedAndTitleContainingOrderByPublishDateDesc(
                            newsTypeId, 1, cleanKeyword);

            // 搜尋特定分類中，內容包含關鍵字的消息
            List<News> contentResults = newsRepository
                    .findByNewsType_NewsTypeIdAndIsPublishedAndContentContainingOrderByPublishDateDesc(
                            newsTypeId, 1, cleanKeyword);

            // 合併結果並去重
            Set<Integer> newsIds = new HashSet<>();
            List<News> result = new ArrayList<>();

            for (News news : titleResults) {
                if (!newsIds.contains(news.getNewsId())) {
                    newsIds.add(news.getNewsId());
                    result.add(news);
                }
            }

            for (News news : contentResults) {
                if (!newsIds.contains(news.getNewsId())) {
                    newsIds.add(news.getNewsId());
                    result.add(news);
                }
            }

            return result;
        }
        return getAllPublishedNews();
    }

    @Override
    public NewsType saveNewsType(NewsType newsType) {
        return newsTypeRepository.save(newsType);
    }

    @Override
    public void deleteNewsType(Integer id) {
        newsTypeRepository.deleteById(id);
    }

    @Override
    public long countNewsByTypeId(Integer newsTypeId) {
        return newsRepository.countByNewsType_NewsTypeId(newsTypeId);
    }
}