package com.petguardian.chat.config;

import io.hypersistence.tsid.TSID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Distributed ID Generation.
 * 
 * Provides a TSID (Time-Sorted Unique Identifier) Factory bean.
 * TSIDs are 64-bit integers (stored as CHAR(13)) that are:
 * 1. Time-sortable (like UUIDv7)
 * 2. High-performance (faster indexing than random UUIDs)
 * 3. K-sortable for efficient database clustering
 */
@Configuration
public class TsidConfig {

    /**
     * Creates a TSID Factory instance.
     * 
     * Configuration:
     * - Node Bits: 10 (Allows for 1024 unique nodes/shards)
     * - This setting is suitable for current single-node MVP and future horizontal
     * scaling.
     */
    @Bean
    public TSID.Factory tsidFactory() {
        return TSID.Factory.builder()
                .withNodeBits(10)
                .build();
    }
}
