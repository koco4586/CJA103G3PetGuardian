package com.petguardian.forum.redis;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.petguardian.forum.service.RedisService;

@Component
public class PostRankingSetupListener {
	
	private final RedisService redisService;

	public PostRankingSetupListener(RedisService redisService) {
		super();
		this.redisService = redisService;
	}
	
	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		redisService.setPostViewsToRedis();
	}
	
}
