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

import com.news.model.News;
import com.news.model.NewsService;

@RestController
@RequestMapping("/api/news") // 統一 API 路徑前綴
public class NewsController {

    @Autowired
    private NewsService newsService;

    // 1. 取得所有消息
    @GetMapping
    public ResponseEntity<List<News>> listAll() {
        return ResponseEntity.ok(newsService.getAllNews());
    }

    // 2. 取得單一消息
    @GetMapping("/{id}")
    public ResponseEntity<News> getOne(@PathVariable Integer id) {
        News news = newsService.getNewsById(id);
        return news != null ? ResponseEntity.ok(news) : ResponseEntity.notFound().build();
    }

    // 3. 新增消息
    @PostMapping
    public ResponseEntity<News> create(@RequestBody News news) {
        return ResponseEntity.ok(newsService.addNews(news));
    }

    // 4. 修改消息
    @PutMapping("/{id}")
    public ResponseEntity<News> update(@PathVariable Integer id, @RequestBody News news) {
        news.setNewsId(id);
        return ResponseEntity.ok(newsService.updateNews(news));
    }

    // 5. 刪除消息
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        newsService.deleteNews(id);
        return ResponseEntity.noContent().build();
    }
}