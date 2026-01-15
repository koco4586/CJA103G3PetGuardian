package com.news.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.news.model.NewsService;
import com.news.model.NewsVO;

@RestController
@RequestMapping("/api/news") // 統一 API 路徑前綴
public class NewsController {

    @Autowired
    private NewsService newsService;

    // 前台：取得已發布消息
    @GetMapping("/active")
    public ResponseEntity<List<NewsVO>> listActive() {
        return ResponseEntity.ok(newsService.getPublishedNews());
    }

    // 後台：取得所有消息
    @GetMapping("/admin/all")
    public ResponseEntity<List<NewsVO>> listAll() {
        return ResponseEntity.ok(newsService.getAllNews());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsVO> getOne(@PathVariable Integer id) {
        NewsVO news = newsService.getNewsById(id);
        return news != null ? ResponseEntity.ok(news) : ResponseEntity.notFound().build();
    }

    @PostMapping("/admin")
    public ResponseEntity<NewsVO> create(@RequestBody NewsVO news) {
        return ResponseEntity.ok(newsService.addNews(news));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<NewsVO> update(@PathVariable Integer id, @RequestBody NewsVO news) {
        return ResponseEntity.ok(newsService.updateNews(id, news));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        newsService.deleteNews(id);
        return ResponseEntity.noContent().build();
    }
}