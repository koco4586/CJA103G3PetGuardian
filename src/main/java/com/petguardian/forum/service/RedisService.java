package com.petguardian.forum.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.petguardian.forum.model.ForumPostVO;

@Service
public class RedisService {
	
	private final StringRedisTemplate redisTemplate;
	private final ForumPostService forumPostService;

	public RedisService(StringRedisTemplate redisTemplate, ForumPostService forumPostService) {
		super();
		this.redisTemplate = redisTemplate;
		this.forumPostService = forumPostService;
	}

	public void incrementPostViewCount(Integer postId) {
		String key = "post:views:" + postId;
		redisTemplate.opsForValue().increment(key);
		
		// 熱門貼文排行榜用
		redisTemplate.opsForZSet().incrementScore("post:rank:", postId.toString(), 1);
	}
	
	public Integer getPostViewCount(Integer postId) {

	    String key = "post:views:" + postId;
	    String value = redisTemplate.opsForValue().get(key);

	    if (value != null) {
	        return Integer.valueOf(value);
	    }

	    return forumPostService.getOnePost(postId).getPostViews();
	    
	}
	
	public List<Integer> getTopHotPostIds(int topN){
		
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
		
	}
	
	public void setPostViewsToRedis() {
		
		List<ForumPostVO> posts = forumPostService.getAllPosts();
		
		posts.forEach(post -> {
			redisTemplate.opsForZSet().add("post:rank:", post.getPostId().toString(), post.getPostViews());
		});
		
	}
	
}
