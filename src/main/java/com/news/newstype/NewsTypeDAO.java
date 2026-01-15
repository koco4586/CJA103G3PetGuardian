package com.news.newstype;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsTypeDAO extends JpaRepository<NewsTypeVO, Integer> {
}
