package com.petguardian.news.controller;

import com.petguardian.news.model.News;
import com.petguardian.news.model.NewsType;
import com.petguardian.news.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    /**
     * 前台 - 最新消息列表頁
     * URL: /news/list
     */
    @GetMapping("/list")
    public String showNewsList(Model model) {
        // 取得所有已發布的消息
        List<News> newsList = newsService.getAllPublishedNews();

        // 取得所有消息類別，供搜尋下拉選單使用
        List<NewsType> newsTypeList = newsService.getAllNewsTypes();

        // 傳遞資料給前端模板
        model.addAttribute("newsList", newsList);
        model.addAttribute("newsTypeList", newsTypeList);

        // 回傳模板路徑
        return "frontend/news";
    }

    /**
     * 前台 - 搜尋消息
     * URL: /news/search
     */
    @GetMapping("/search")
    public String searchNews(
            @RequestParam(required = false) Integer newsTypeId,
            @RequestParam(required = false) String keyword,
            Model model) {

        // 執行搜尋
        List<News> newsList = newsService.searchNews(newsTypeId, keyword);

        // 取得所有消息類別，供搜尋下拉選單使用
        List<NewsType> newsTypeList = newsService.getAllNewsTypes();

        // 傳遞資料給前端模板
        model.addAttribute("newsList", newsList);
        model.addAttribute("newsTypeList", newsTypeList);

        // 保留搜尋條件，方便使用者看到自己搜了什麼
        model.addAttribute("searchNewsTypeId", newsTypeId);
        model.addAttribute("searchKeyword", keyword);

        // 傳遞搜尋結果數量
        model.addAttribute("searchResultCount", newsList.size());

        // 回傳模板路徑
        return "frontend/news";
    }

    /**
     * 前台 - 消息詳情頁
     * URL: /news/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public String showNewsDetail(@PathVariable Integer id, Model model) {
        // 根據 ID 查詢消息
        News news = newsService.getNewsById(id).orElse(null);

        // 檢查消息是否存在
        if (news == null) {
            model.addAttribute("errorMessage", "查無該消息");
            return "frontend/news/list";
        }

        // 確認是否已發布
        if (news.getIsPublished() != 1) {
            model.addAttribute("errorMessage", "此消息尚未發布或已下架");
            return "frontend/news/list";
        }

        // 傳遞資料給前端模板
        model.addAttribute("news", news);

        // 回傳模板路徑
        return "frontend/news/news-one-post";
    }
}