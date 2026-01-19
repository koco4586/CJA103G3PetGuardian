package com.petguardian.news.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Integer> {

    // 查詢已發布的消息（按發布日期降冪排序）
    List<News> findByIsPublishedOrderByPublishDateDesc(Integer isPublished);

    // 查詢特定類別的消息
    List<News> findByNewsType_NewsTypeIdAndIsPublishedOrderByPublishDateDesc(
            Integer newsTypeId, Integer isPublished);
}