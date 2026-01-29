package com.petguardian.news.controller;

import com.petguardian.news.model.News;
import com.petguardian.news.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    /**
     * 前台 - 最新消息列表頁
     * URL: /news/list
     * 模板: templates/frontend/news.html
     */
    @GetMapping("/list")
    public String showNewsList(Model model) {
        // 取得所有已發布的消息
        List<News> newsList = newsService.getAllPublishedNews();

        // 傳遞資料給前端模板
        model.addAttribute("newsList", newsList);

        // 回傳模板路徑
        return "frontend/news";
    }

    /**
     * 前台 - 消息詳情頁
     * URL: /news/detail/{id}
     * 模板: templates/frontend/news/news-detail.html
     */
    @GetMapping("/detail/{id}")
    public String showNewsDetail(@PathVariable Integer id, Model model) {
        // 根據 ID 查詢消息
        News news = newsService.getNewsById(id)
                .orElseThrow(() -> new RuntimeException("消息不存在"));

        // 確認是否已發布
        if (news.getIsPublished() != 1) {
            throw new RuntimeException("此消息尚未發布");
        }

        // 傳遞資料給前端模板
        model.addAttribute("news", news);

        // 回傳模板路徑
        return "frontend/news/news-one-post";
    }
}
