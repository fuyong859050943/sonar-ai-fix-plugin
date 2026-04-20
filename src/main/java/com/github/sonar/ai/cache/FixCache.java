/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.cache;

import com.github.sonar.ai.fix.FixSuggestion;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.sonar.api.server.ServerSide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 修复建议缓存
 * 
 * 使用 Caffeine 缓存避免重复调用 LLM API，
 * 节省 Token 消耗并提高响应速度。
 */
@ServerSide
public class FixCache {

    private static final Logger LOG = LoggerFactory.getLogger(FixCache.class);

    // 默认缓存配置
    private static final int DEFAULT_MAX_SIZE = 1000;
    private static final int DEFAULT_EXPIRE_MINUTES = 60;

    private final Cache<String, FixSuggestion> cache;

    /**
     * 构造函数（使用默认配置）
     */
    public FixCache() {
        this(DEFAULT_MAX_SIZE, DEFAULT_EXPIRE_MINUTES);
    }

    /**
     * 构造函数
     * 
     * @param maxSize 最大缓存条目数
     * @param expireMinutes 过期时间（分钟）
     */
    public FixCache(int maxSize, int expireMinutes) {
        this.cache = Caffeine.newBuilder()
            .maximumSize(maxSize)
            .expireAfterWrite(expireMinutes, TimeUnit.MINUTES)
            .recordStats() // 记录缓存统计
            .build();

        LOG.info("FixCache initialized: maxSize={}, expireAfter={}min", maxSize, expireMinutes);
    }

    /**
     * 生成缓存 Key
     * 
     * @param issueKey 问题 Key
     * @param codeSnippet 代码片段（可选）
     * @return 缓存 Key
     */
    public String generateKey(String issueKey, String codeSnippet) {
        // 使用 issueKey 作为主要 key
        // 如果代码片段变化，应该生成新的建议
        if (codeSnippet != null && !codeSnippet.isEmpty()) {
            int hash = (issueKey + codeSnippet).hashCode();
            return "fix:" + issueKey + ":" + hash;
        }
        return "fix:" + issueKey;
    }

    /**
     * 获取缓存的修复建议
     * 
     * @param key 缓存 Key
     * @return 修复建议（如果存在）
     */
    public Optional<FixSuggestion> get(String key) {
        FixSuggestion suggestion = cache.getIfPresent(key);
        if (suggestion != null) {
            LOG.debug("Cache hit for key: {}", key);
            return Optional.of(suggestion);
        }
        LOG.debug("Cache miss for key: {}", key);
        return Optional.empty();
    }

    /**
     * 缓存修复建议
     * 
     * @param key 缓存 Key
     * @param suggestion 修复建议
     */
    public void put(String key, FixSuggestion suggestion) {
        cache.put(key, suggestion);
        LOG.debug("Cached suggestion for key: {}", key);
    }

    /**
     * 使缓存失效
     * 
     * @param key 缓存 Key
     */
    public void invalidate(String key) {
        cache.invalidate(key);
        LOG.debug("Invalidated cache for key: {}", key);
    }

    /**
     * 清空所有缓存
     */
    public void invalidateAll() {
        cache.invalidateAll();
        LOG.info("All cache invalidated");
    }

    /**
     * 获取缓存统计信息
     * 
     * @return 统计信息字符串
     */
    public String getStats() {
        var stats = cache.stats();
        return String.format(
            "CacheStats{hitCount=%d, missCount=%d, hitRate=%.2f%%, evictionCount=%d, size=%d}",
            stats.hitCount(),
            stats.missCount(),
            stats.hitRate() * 100,
            stats.evictionCount(),
            cache.estimatedSize()
        );
    }

    /**
     * 获取缓存大小
     * 
     * @return 当前缓存条目数
     */
    public long size() {
        return cache.estimatedSize();
    }

    /**
     * 获取命中率
     * 
     * @return 命中率（0-1）
     */
    public double hitRate() {
        return cache.stats().hitRate();
    }
}
