package com.petguardian.forum.redis;

import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.petguardian.forum.model.ForumPostRepository;
import com.petguardian.forum.service.ForumPostService;
import com.petguardian.forum.service.RedisService;

@Component
@EnableScheduling
public class PostViewSyncTask {

	private final RedisService redisService;
	private final StringRedisTemplate redisTemplate;
	private final ForumPostService forumPostService;
	private final ForumPostRepository repo;

	public PostViewSyncTask(RedisService redisService, StringRedisTemplate redisTemplate,
			ForumPostService forumPostService, ForumPostRepository repo) {
		super();
		this.redisService = redisService;
		this.redisTemplate = redisTemplate;
		this.forumPostService = forumPostService;
		this.repo = repo;
	}

	// 每小時執行一次 (cron 表示式)
    @Scheduled(cron = "0 0 * * * *")
	public void syncPostViewCountToDatabase() {
		
		Set<String> keys = redisTemplate.keys("post:views:*");
		
		if(keys == null || keys.isEmpty()) {
			return;
		}
		
		keys.stream()
			.map(key -> {
				Integer postId = Integer.valueOf(key.split(":")[2]);
				Integer postViewCount = redisService.getPostViewCount(postId);
				return (postViewCount != null) ? Map.entry(postId, postViewCount) : null;
			})
			.filter(entry -> entry != null)
			.filter(entry -> entry.getValue() != 0)
			.filter(entry -> {
				Integer dbValue = forumPostService.getOnePost(entry.getKey()).getPostViews();
				return !entry.getValue().equals(dbValue);
			})
			.forEach(entry -> {
				repo.savePostViewCountToDatabase(entry.getKey(), entry.getValue());
			});		
	}
    	
}
