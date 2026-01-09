package com.news.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsDAO extends JpaRepository<News, Integer> {
    // 若有特殊查詢需求可定義於此，例如：
    List<News> findByTitleContaining(String keyword);
}