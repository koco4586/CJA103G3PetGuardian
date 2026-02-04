package com.petguardian.news.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Integer> {

    // 查詢已發布的消息，按發布日期降冪排序
    List<News> findByIsPublishedOrderByPublishDateDesc(Integer isPublished);

    // 查詢特定類別的已發布消息
    List<News> findByNewsType_NewsTypeIdAndIsPublishedOrderByPublishDateDesc(
            Integer newsTypeId, Integer isPublished);

    // 根據標題關鍵字搜尋已發布的消息
    List<News> findByIsPublishedAndTitleContainingOrderByPublishDateDesc(
            Integer isPublished, String keyword);

    // 根據內容關鍵字搜尋已發布的消息
    List<News> findByIsPublishedAndContentContainingOrderByPublishDateDesc(
            Integer isPublished, String keyword);

    // 根據標題或內容關鍵字搜尋已發布的消息
    List<News> findByIsPublishedAndTitleContainingOrIsPublishedAndContentContainingOrderByPublishDateDesc(
            Integer isPublished1, String titleKeyword,
            Integer isPublished2, String contentKeyword);

    // 根據分類和標題關鍵字搜尋
    List<News> findByNewsType_NewsTypeIdAndIsPublishedAndTitleContainingOrderByPublishDateDesc(
            Integer newsTypeId, Integer isPublished, String keyword);

    // 根據分類和內容關鍵字搜尋
    List<News> findByNewsType_NewsTypeIdAndIsPublishedAndContentContainingOrderByPublishDateDesc(
            Integer newsTypeId, Integer isPublished, String keyword);

    // 計算某類別下的消息數量
    long countByNewsType_NewsTypeId(Integer newsTypeId);

    // 計算已發布的消息數量
    long countByIsPublished(Integer isPublished);
}