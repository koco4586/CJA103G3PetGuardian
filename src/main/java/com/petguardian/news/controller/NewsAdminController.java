package com.petguardian.news.controller;

import com.petguardian.news.model.News;
import com.petguardian.news.model.NewsType;
import com.petguardian.news.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/news")
public class NewsAdminController {

    @Autowired
    private NewsService newsService;

    /**
     * 後台 - 消息管理列表頁
     * URL: /admin/news/list
     * 模板: templates/backend/news.html
     */
    @GetMapping("/list")
    public String showNewsManagement(Model model) {
        // 取得所有消息（包含草稿）
        List<News> newsList = newsService.getAllNews();

        // 取得所有消息類別（供新增/編輯時選擇）
        List<NewsType> newsTypeList = newsService.getAllNewsTypes();

        model.addAttribute("newsList", newsList);
        model.addAttribute("newsTypeList", newsTypeList);

        // 回傳模板路徑
        return "backend/news";
    }

    /**
     * 後台 - 新增/更新消息
     * URL: POST /admin/news/save
     */
    @PostMapping("/save")
    public String saveNews(
            @RequestParam(required = false) Integer newsId,
            @RequestParam String title,
            @RequestParam Integer newsTypeId,
            @RequestParam String content,
            @RequestParam String publishDate,
            @RequestParam Integer isPublished,
            RedirectAttributes redirectAttributes) {

        News news;

        if (newsId != null && newsId > 0) {
            // 更新現有消息
            news = newsService.getNewsById(newsId)
                    .orElseThrow(() -> new RuntimeException("消息不存在"));
        } else {
            // 新增消息
            news = new News();
            news.setAdmId(2001); // TODO: 從 Session 取得登入的管理員 ID
        }

        // 設定基本資料
        news.setTitle(title);
        news.setContent(content);
        news.setIsPublished(isPublished);

        // 設定消息類別（透過 ID 取得物件）
        NewsType newsType = newsService.getNewsTypeById(newsTypeId)
                .orElseThrow(() -> new RuntimeException("消息類別不存在"));
        news.setNewsType(newsType);

        // 解析發布日期
        news.setPublishDate(LocalDateTime.parse(publishDate));

        // 儲存到資料庫
        newsService.saveNews(news);

        redirectAttributes.addFlashAttribute("successMessage", "消息儲存成功！");
        return "redirect:/admin/news/list";
    }

    /**
     * 後台 - 刪除消息
     * URL: POST /admin/news/delete
     */
    @PostMapping("/delete")
    public String deleteNews(
            @RequestParam Integer id,
            RedirectAttributes redirectAttributes) {

        newsService.deleteNews(id);

        redirectAttributes.addFlashAttribute("successMessage", "消息已刪除！");
        return "redirect:/admin/news/list";
    }
}