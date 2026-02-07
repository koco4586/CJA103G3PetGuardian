package com.petguardian.forum.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.petguardian.forum.model.ForumPostVO;
import com.petguardian.forum.model.ForumVO;

@Service
public class RedisService {
	
	private final StringRedisTemplate redisTemplate;
	private final ForumPostService forumPostService;
	private final ForumService forumService;

	public RedisService(StringRedisTemplate redisTemplate, ForumPostService forumPostService,
			ForumService forumService) {
		super();
		this.redisTemplate = redisTemplate;
		this.forumPostService = forumPostService;
		this.forumService = forumService;
	}

	public void incrementPostViewCount(Integer postId) {
		try {
			String key = "post:views:" + postId;
			redisTemplate.opsForValue().increment(key);
			// 熱門貼文排行榜用
			redisTemplate.opsForZSet().incrementScore("post:rank:", postId.toString(), 1);
		}catch(Exception e){
			System.err.println("Redis 連線失敗，請檢查 Redis 是否已啟動。");
		}
	}
	
	public void incrementForumViewCount(Integer forumId) {
		try {
			String key = "forum:views:" + forumId;
			redisTemplate.opsForValue().increment(key);
			redisTemplate.opsForZSet().incrementScore("forum:rank:", forumId.toString(), 1);
		}catch(Exception e){
			System.err.println("Redis 連線失敗，請檢查 Redis 是否已啟動。");
		}
	}
	
	public Integer getPostViewCount(Integer postId) {
		try {
		    String key = "post:views:" + postId;
		    String value = redisTemplate.opsForValue().get(key);	
		    if (value != null) {
		        return Integer.valueOf(value);
		    }
		}catch(Exception e){
			System.err.println("Redis 連線失敗，請檢查 Redis 是否已啟動。");
		}
		return forumPostService.getOnePost(postId).getPostViews();
	}
	
	public Integer getForumViewCount(Integer forumId) {
		try {
			String key = "forum:views:" + forumId;
			String value = redisTemplate.opsForValue().get(key);
			if(value != null) {
				return Integer.valueOf(value);
			}
		}catch(Exception e) {
			System.err.println("Redis 連線失敗，請檢查 Redis 是否已啟動。");
		}
		return forumService.getOneForum(forumId).getForumViews();
	}
	
	public List<Integer> getTopHotPostIds(int topN){
		try {
			Set<String> postIds = redisTemplate.opsForZSet().reverseRange("post:rank:", 0, topN - 1);
			// 防止NullPointerException
			if(postIds.isEmpty() || postIds == null) {
				return Collections.emptyList();
			}
			return postIds.stream()
					.map(postId -> {
						return Integer.valueOf(postId);
					})
					.collect(Collectors.toList());
		}catch(Exception e){
			System.err.println("Redis 連線失敗，請檢查 Redis 是否已啟動。");
		}
		return Collections.emptyList();
	}
	
	public List<Integer> getTopHotForumIds(int topN){
		try {
			Set<String> forumIds =  redisTemplate.opsForZSet().reverseRange("forum:rank:", 0, topN - 1);
			if(forumIds == null || forumIds.isEmpty()) {
				return Collections.emptyList();
			}
			return forumIds.stream()
					.map(forumId -> {
						return Integer.valueOf(forumId);
					})
					.collect(Collectors.toList());	
		}catch(Exception e) {
			System.err.println("Redis 連線失敗，請檢查 Redis 是否已啟動。");
		}
		return Collections.emptyList();
	}
	
	public void setPostViewsToRedis() {
		List<ForumPostVO> posts = forumPostService.getAllPosts();
		posts.forEach(post -> {
			redisTemplate.opsForZSet().add("post:rank:", post.getPostId().toString(), post.getPostViews());
		});
	}
	
	public void setForumViewsToRedis() {
		List<ForumVO> forums = forumService.getAll();
		forums.forEach(forum -> {
			redisTemplate.opsForZSet().add("forum:rank:", forum.getForumId().toString(), forum.getForumViews());
		});
	}
	
}
