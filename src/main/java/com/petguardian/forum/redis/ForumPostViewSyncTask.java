package com.petguardian.forum.redis;

import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.petguardian.forum.model.ForumPostRepository;
import com.petguardian.forum.model.ForumRepository;
import com.petguardian.forum.service.RedisService;

@Component
@EnableScheduling
public class ForumPostViewSyncTask {

	private final RedisService redisService;
	private final StringRedisTemplate redisTemplate;
	private final ForumPostRepository postRepo;
	private final ForumRepository forumRepo;

	public ForumPostViewSyncTask(RedisService redisService, StringRedisTemplate redisTemplate, ForumPostRepository postRepo,
			ForumRepository forumRepo) {
		super();
		this.redisService = redisService;
		this.redisTemplate = redisTemplate;
		this.postRepo = postRepo;
		this.forumRepo = forumRepo;
	}

	// 每小時執行一次 (cron 表示式)
//	@Scheduled(cron = "0 0 * * * *")
	@Scheduled(fixedRate = 60000)
	public void syncPostViewCountToDatabase() {
		try {
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
				.filter(entry -> entry != null && entry.getValue() != 0)
				.forEach(entry -> {
					postRepo.savePostViewCountToDatabase(entry.getKey(), entry.getValue());
				});
		}catch(Exception e) {
			System.err.println("Redis 連線失敗，請檢查 Redis 是否已啟動。");
		}
	}
    
    @Scheduled(fixedRate = 60000)
    public void syncForumViewCountToDatabase() {
    	try {
	    	Set<String> keys = redisTemplate.keys("forum:views:*");
	    	
	    	if(keys == null || keys.isEmpty()) return;
	    	
	    	keys.stream()
	    		.map(key -> {
	    			Integer forumId = Integer.valueOf(key.split(":")[2]);
	    			Integer forumViewCount = redisService.getForumViewCount(forumId);
	    			return (forumViewCount != null) ? Map.entry(forumId, forumViewCount) : null;
	    		})
	    		.filter(entry -> entry != null && entry.getValue() != 0)
	    		.forEach(entry -> {
	    			forumRepo.saveForumViewCountToDatabase(entry.getKey(), entry.getValue());
	    		});
    	}catch(Exception e) {
    		System.err.println("Redis 連線失敗，請檢查 Redis 是否已啟動。");
    	}
    	
    }
    	
}
