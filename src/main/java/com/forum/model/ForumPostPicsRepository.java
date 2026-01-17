package com.forum.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ForumPostPicsRepository extends JpaRepository<ForumPostPicsVO, Integer> {

	//	找到該篇貼文的所有picId
	@Query(value = "select p.picId from ForumPostPicsVO p where p.forumPost.postId = :postId")
	public List<Integer> findPicsIdByPostId(@Param("postId") Integer postId);
	
	//	再從前端跑回圈傳回picId，拿到所有照片(一次只能傳回一張)
	@Query(value = "select p.pic from ForumPostPicsVO p where p.picId = :picId")
	public byte[] findPicByPicId(@Param("picId") Integer picId);
	
}
