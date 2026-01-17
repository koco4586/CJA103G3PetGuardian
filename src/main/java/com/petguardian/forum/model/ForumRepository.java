package com.petguardian.forum.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

public interface ForumRepository extends JpaRepository<ForumVO, Integer> {

	// 關鍵字查詢
	@Query(value = "select f from ForumVO f where f.forumStatus = 1 and f.forumName like concat('%', :forumName, '%') order by f.forumId desc")
	public List<ForumVO> findByForumName(@Param("forumName") String forumName);

	// 取得啟用討論區 Spring Data JPA 方法命名 會自動生成JPQL指令
	// List<ForumVO> findByForumStatusOrderByForumIdDesc(Integer forumStatus);

	// 取得啟用討論區 用JPQL指令
	@Query(value = "select f from ForumVO f where f.forumStatus = 1 order by f.forumId desc")
	public List<ForumVO> getAllActive();

	// 更新討論區狀態
	@Transactional
	@Modifying
	@Query(value = "update ForumVO f set f.forumStatus = :forumStatus where f.forumId = :forumId")
	public void updateStatus(@Param("forumStatus") Integer forumStatus, @Param("forumId") Integer forumId);

	// 只拿圖片方法
	@Query(value = "select f.forumPic from ForumVO f where f.forumId = :forumId")
	public byte[] getPicture(@Param("forumId") Integer forumId);

}
