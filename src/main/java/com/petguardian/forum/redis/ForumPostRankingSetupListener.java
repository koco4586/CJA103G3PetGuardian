package com.petguardian.forum.redis;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.petguardian.forum.service.RedisService;

@Component
public class ForumPostRankingSetupListener {
	
	private final RedisService redisService;

	public ForumPostRankingSetupListener(RedisService redisService) {
		super();
		this.redisService = redisService;
	}
	
	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		try {	
			redisService.setPostViewsToRedis();
			redisService.setForumViewsToRedis();
		}catch(Exception e) {
			System.err.println("Redis 連線失敗，請檢查 Redis 是否已啟動。");
		}
	}	
	
}
