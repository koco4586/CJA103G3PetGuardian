package com.petguardian.forum.service;

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
	}
	
	public Integer getPostViewCount(Integer postId) {

	    String key = "post:views:" + postId;
	    String value = redisTemplate.opsForValue().get(key);

	    if (value != null) {
	        return Integer.valueOf(value);
	    }

	    ForumPostVO forumPostVO = forumPostService.getOnePost(postId);
	    return Integer.valueOf(forumPostVO.getPostViews());
	    
	}
	
}
