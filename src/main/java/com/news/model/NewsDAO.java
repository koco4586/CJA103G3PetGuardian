package com.news.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsDAO extends JpaRepository<NewsVO, Integer> {
    // 查詢已發布的消息，並按時間倒序排列 (給前台用)
    List<NewsVO> findByIsPublishedOrderByCreatedTimeDesc(Integer isPublished);
}