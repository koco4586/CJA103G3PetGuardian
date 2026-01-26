package com.petguardian.news.controller;

import com.petguardian.news.model.NewsType;
import com.petguardian.news.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/news/type")
public class NewsTypeAdminController {

    @Autowired
    private NewsService newsService;

    /**
     * 新增消息類別
     * URL: POST /admin/news/type/save
     */
    @PostMapping("/save")
    public String saveNewsType(
            @RequestParam(required = false) Integer newsTypeId,
            @RequestParam String newsTypeName,
            RedirectAttributes redirectAttributes) {

        try {
            NewsType newsType;

            if (newsTypeId != null && newsTypeId > 0) {
                // 更新現有類別
                newsType = newsService.getNewsTypeById(newsTypeId)
                        .orElseThrow(() -> new RuntimeException("類別不存在"));
                newsType.setNewsTypeName(newsTypeName);
            } else {
                // 新增類別
                newsType = new NewsType();
                newsType.setNewsTypeName(newsTypeName);
            }

            newsService.saveNewsType(newsType);
            redirectAttributes.addFlashAttribute("successMessage", "類別儲存成功！");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "儲存失敗：" + e.getMessage());
        }

        return "redirect:/admin/news/list";
    }

    /**
     * 刪除消息類別
     * URL: POST /admin/news/type/delete
     */
    @PostMapping("/delete")
    public String deleteNewsType(
            @RequestParam Integer id,
            RedirectAttributes redirectAttributes) {

        try {
            // 檢查是否有消息使用此類別
            long count = newsService.countNewsByTypeId(id);

            if (count > 0) {
                redirectAttributes.addFlashAttribute("error",
                        "無法刪除：此類別下還有 " + count + " 則消息，請先刪除或移動這些消息");
            } else {
                newsService.deleteNewsType(id);
                redirectAttributes.addFlashAttribute("successMessage", "類別已刪除！");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "刪除失敗：" + e.getMessage());
        }

        return "redirect:/admin/news/list";
    }
}